package com.tcoded.playtime.placeholderapi;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.tcoded.playtime.manager.PlayerDataManager;
import com.tcoded.playtime.PlayTime;
import com.tcoded.playtime.utils.PlayerPlayTimeData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlayTimeExpansion extends PlaceholderExpansion {
    private final PlayTime plugin;
    private final PlayerDataManager playerDataManager;
//    static Pattern topPlaceholder = Pattern.compile("top_([0-9]+)_(name|time)");
//    Pattern positionPlaceholder = Pattern.compile("position_([_A-Za-z0-9]+)");

    public PlayTimeExpansion(PlayTime plugin) {
        this.plugin = plugin;
        this.playerDataManager = this.plugin.getPlayerDataManager();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "playtime";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String commandLabel) {
        // General
        if (commandLabel.equals("serveruptime"))
            return String.valueOf(plugin.getTimeFormatUtil().getUptime());
        else if (commandLabel.equals("position")) {
            return "TODO-NotImplemented";
        }
        else if (commandLabel.startsWith("top_")) {
            return "TODO-NotImplemented";
        }

        // Per player
        PlayerPlayTimeData playerData = this.playerDataManager.getCachedData(offlinePlayer.getUniqueId());
        if (playerData == null) {
            if (!offlinePlayer.isOnline() && !Bukkit.isPrimaryThread()) {
                playerData = this.playerDataManager.getOrLoadData(offlinePlayer.getUniqueId(), false);
            } else {
                return "player-data-null";
            }
        }

        Duration durationPlayed = this.getDurationPlayed(playerData);
        if (commandLabel.equals("playername")) {
            return String.valueOf(offlinePlayer.getName());
        }
        else if (commandLabel.equals("playtime")) {
            return this.plugin.getTimeFormatUtil().formatTime(durationPlayed);
        }
        else if (commandLabel.equals("playtime_leaderboard")) {
            return this.plugin.getTimeFormatUtil().formatTime(durationPlayed, false, false, true, true, false);
        }
        else if (commandLabel.equals("lastjoin")) {
            return this.plugin.getTimeFormatUtil().formatTime(
                    Duration.ofMillis(System.currentTimeMillis() - playerData.getLastJoinTime()));
        }
        else if (commandLabel.equals("lastquit")) {
            return this.plugin.getTimeFormatUtil().formatTime(
                    Duration.ofMillis(System.currentTimeMillis() - playerData.getLastQuitTime()));
        }
        else if (commandLabel.equals("playtime_seconds")) {
            return String.valueOf(durationPlayed.getSeconds());
        }
        else if (commandLabel.equals("playtime_minutes")) {
            return String.valueOf(durationPlayed.getSeconds() / 60);
        }
        else if (commandLabel.equals("playtime_hours")) {
            return String.valueOf(durationPlayed.getSeconds() / (60 * 60));
        }
        else if (commandLabel.equals("playtime_days")) {
            return String.valueOf(durationPlayed.getSeconds() /  (60 * 60 * 24));
        }
        else if (commandLabel.equals("playtime_weeks")) {
            return String.valueOf(durationPlayed.getSeconds() / (60 * 60 * 24 * 7));
        }
        else if (commandLabel.equals("session")) {
            PlayerPlayTimeData data = this.playerDataManager.getOrLoadData(offlinePlayer.getUniqueId());

            if (data == null) return "internal-error";

            long joinTime = data.getLastJoinTime();
            long now = System.currentTimeMillis();
            long delta = now - joinTime;

            return this.plugin.getTimeFormatUtil().formatTime(Duration.of(delta, ChronoUnit.MILLIS));
        }
        if (commandLabel.equals("timesjoined")) {
            return String.valueOf(playerData.getJoinCount());
        }
        return null;
    }

    private Duration getDurationPlayed(PlayerPlayTimeData data) {
        return Duration.of(getSecondsPlayed(data), ChronoUnit.SECONDS);
    }

    private long getSecondsPlayed(PlayerPlayTimeData data) {
        UUID uuid = data.getUuid();
        Player player = this.plugin.getServer().getPlayer(uuid);

        if (player != null) {
            return playerDataManager.getStatsSeconds(player);
        } else {
            return data.getSecondsPlayed();
        }
    }
}
