package com.tcoded.playtime.manager;

import com.tcoded.playtime.PlayTime;
import com.tcoded.playtime.utils.CompatibilityUtil;
import com.tcoded.playtime.utils.FileUtil;
import com.tcoded.playtime.utils.PlayerPlayTimeData;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    public static final String PLAYER_DATA_FOLDER_NAME = "playerData";
    public static final String PLAYER_DATA_FILE_FORMAT = "%s.dat";

    private final File playerDataFolder;
    private final ConcurrentHashMap<UUID, PlayerPlayTimeData> dataCacheMap;
    private final ConcurrentHashMap<UUID, Object> dataLocks;
    private final PlayTime plugin;

    public PlayerDataManager(PlayTime plugin) {
        this.plugin = plugin;

        this.playerDataFolder = new File(plugin.getDataFolder(), PLAYER_DATA_FOLDER_NAME);
        if (!this.playerDataFolder.exists()) this.playerDataFolder.mkdirs();

        this.dataCacheMap = new ConcurrentHashMap<>();
        this.dataLocks = new ConcurrentHashMap<>();
    }

    public void loadAll() {
        this.plugin.getServer().getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();
            PlayerPlayTimeData data = this.getOrLoadData(uuid);
            if (data == null) {
                data = this.createNewPlayerDataFile(uuid);
                this.resetPlayerData(player, data);
            }
        });
    }

    private void resetPlayerData(Player player, PlayerPlayTimeData data) {
        data.setLastJoinTime(player.getLastPlayed());
        data.setJoinCount(player.getStatistic(Statistic.LEAVE_GAME) + 1);
        data.setLastQuitTime(0);
        data.setLastKnownName(player.getName());
        this.refreshPlayTime(player, data);

        this.saveDataAsync(player.getUniqueId());
    }

    @Nullable
    public PlayerPlayTimeData getOrLoadData(UUID uuid) {
        return this.getOrLoadData(uuid, true);
    }

    @Nullable
    public PlayerPlayTimeData getOrLoadData(UUID uuid, boolean cache) {
        PlayerPlayTimeData data = this.dataCacheMap.get(uuid);
        if (data != null) return data;

        final Object lock = this.dataLocks.computeIfAbsent(uuid, k -> new Object());

        String rawData;
        try {
            // Prevent simultaneous reads and writes
            synchronized (lock) {
                rawData = FileUtil.readFile(getPlayerDataFile(uuid));
            }
        } catch (FileNotFoundException ignored) {
            return null;
        }

        if (rawData == null) return null;

        data = PlayerPlayTimeData.deserialize(uuid, rawData);

        if (cache) this.dataCacheMap.put(uuid, data);
        else if (plugin.getServer().getPlayer(uuid) == null) this.dataLocks.remove(uuid);

        return data;
    }

    public PlayerPlayTimeData createNewPlayerDataFile(UUID uuid) {
        PlayerPlayTimeData data = new PlayerPlayTimeData(uuid, "", 0, 0, 0, 0);
        this.dataCacheMap.put(uuid, data);
        this.saveData(uuid);

        return data;
    }

    public void saveData(UUID uuid) {
        PlayerPlayTimeData data = this.dataCacheMap.get(uuid);

        final Object lock = this.dataLocks.computeIfAbsent(uuid, k -> new Object());

        try {
            // Prevent simultaneous reads and writes
            synchronized (lock) {
                FileUtil.overwriteFile(this.getPlayerDataFile(uuid), PlayerPlayTimeData.serialize(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unloadData(UUID uuid) {
        this.dataCacheMap.remove(uuid);
        this.dataLocks.remove(uuid);
    }

    @NotNull
    private File getPlayerDataFile(UUID uuid) {
        return new File(this.playerDataFolder, String.format(PLAYER_DATA_FILE_FORMAT, uuid.toString()));
    }

    public CompletableFuture<Void> saveDataAsync(UUID uuid) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            this.saveData(uuid);
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<Optional<PlayerPlayTimeData>> findOfflinePlayerData(UUID uuid) {
        CompletableFuture<Optional<PlayerPlayTimeData>> future = new CompletableFuture<>();

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            PlayerPlayTimeData data = this.getOrLoadData(uuid, false);
            future.complete(Optional.ofNullable(data));
        });

        return future;
    }

    public void refreshPlayTime(Player player, PlayerPlayTimeData data) {
        int ticksPlayed = player.getStatistic(CompatibilityUtil.getTicksPlayedStatEnum());
        int secondsPlayed = ticksPlayed / 20; // 20 TPS
        data.setSecondsPlayed(secondsPlayed);
    }

    public long getStatsSeconds(Player player) {
        return player.getStatistic(CompatibilityUtil.getTicksPlayedStatEnum()) / 20;
    }

    public PlayerPlayTimeData getCachedData(UUID uuid) {
        return this.dataCacheMap.get(uuid);
    }
}
