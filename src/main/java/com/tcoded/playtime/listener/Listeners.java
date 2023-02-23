package com.tcoded.playtime.listener;

import com.tcoded.playtime.PlayTime;
import com.tcoded.playtime.manager.PlayerDataManager;
import com.tcoded.playtime.utils.PlayerPlayTimeData;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class Listeners implements Listener {

    private final PlayTime plugin;

    public Listeners(PlayTime plugin) {
        this.plugin = plugin;
    }

    // Pre-load data to not lock main thread once we move to the pre-join
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            UUID uuid = event.getUniqueId();
            PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
            PlayerPlayTimeData data = playerDataManager.getOrLoadData(uuid);
            if (data == null) data = playerDataManager.createNewPlayerDataFile(uuid);
        }
    }

    // Use login event to ensure no player information was changed yet
    @EventHandler
    public void onPreJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        PlayerPlayTimeData data = playerDataManager.getOrLoadData(uuid);
        if (data == null) return;

        String name = player.getName();
        int joins = player.getStatistic(Statistic.LEAVE_GAME) + 1;
        long lastJoinTime = System.currentTimeMillis();

        data.setLastKnownName(name);
        playerDataManager.refreshPlayTime(player, data);
        data.setJoinCount(joins);
        data.setLastJoinTime(lastJoinTime);

        playerDataManager.saveDataAsync(uuid);
    }

    // No memory leaking :)
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        PlayerDataManager playerDataManager = this.plugin.getPlayerDataManager();
        PlayerPlayTimeData data = playerDataManager.getOrLoadData(uuid);
        if (data == null) return;

        data.setLastQuitTime(System.currentTimeMillis());
        playerDataManager.refreshPlayTime(player, data);

        playerDataManager.saveDataAsync(uuid).thenRun(() -> {
            playerDataManager.unloadData(uuid);
        });

    }

}
