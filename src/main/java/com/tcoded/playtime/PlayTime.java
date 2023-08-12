package com.tcoded.playtime;

import com.tcoded.playtime.commands.PlayTimeCmd;
import com.tcoded.playtime.manager.PlayerDataManager;
import com.tcoded.playtime.placeholderapi.PlayTimeExpansion;
import com.tcoded.playtime.utils.TimeFormat;
import com.tcoded.playtime.listener.Listeners;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.tcoded.playtime.utils.ChatUtil;
import com.tcoded.playtime.utils.UpdateChecker;

public class PlayTime extends JavaPlugin implements Listener {

    private final int SPIGOT_RESOURCE_ID = 26016;

    private PlayerDataManager playerDataManager;
    private ChatUtil chatUtil;
    private TimeFormat formatTimeUtil;


    @Override
    public void onEnable() {
        // Config
        this.saveDefaultConfig();

        // Util
        this.chatUtil = new ChatUtil(this);
        this.formatTimeUtil = new TimeFormat(this);

        // Managers
        this.playerDataManager = new PlayerDataManager(this);
        this.playerDataManager.loadAll();

        // Commands
        PlayTimeCmd playtimeCmd = new PlayTimeCmd(this);
        PluginCommand playtimePlCmd = getCommand("playtime");
        playtimePlCmd.setExecutor(playtimeCmd);
        playtimePlCmd.setTabCompleter(playtimeCmd);

        // Listeners
        this.getServer().getPluginManager().registerEvents(new Listeners(this), this);

        // Placeholders
        placeholderAPI();

        // Tasks
        updateChecker();
    }

    private void updateChecker() {
        this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            new UpdateChecker(this, SPIGOT_RESOURCE_ID).getVersion(version -> {
                String currVersion = getDescription().getVersion();
                if (currVersion.equalsIgnoreCase(version)) {
                    getChatUtil().console("%prefix% &fLatest version is &ainstalled&7! - v" + currVersion);
                } else {
                    getChatUtil().console("%prefix% &cUpdate available! &7- v" + currVersion + " -> v" + version);
                }
            });
        });
    }

    private void placeholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getChatUtil().console("%prefix% &bPlaceholderAPI &awas found&7! Registering Placeholders.");
            new PlayTimeExpansion(this).register();
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            getChatUtil().console("%prefix% &bPlaceholderAPI &cwas not found&7! Disabling Plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().stream().map(Player::getUniqueId).forEach(playerDataManager::saveData);
    }

    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    public TimeFormat getTimeFormatUtil() {
        return this.formatTimeUtil;
    }

    public ChatUtil getChatUtil() {
        return this.chatUtil;
    }
}