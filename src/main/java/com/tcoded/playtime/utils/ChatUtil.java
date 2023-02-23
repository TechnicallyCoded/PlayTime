package com.tcoded.playtime.utils;

import com.tcoded.playtime.PlayTime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;

public class ChatUtil {
    private final PlayTime plugin;

    public ChatUtil(PlayTime plugin) {
        this.plugin = plugin;
    }

    public void message(CommandSender sender, Player player, String message) {
        sender.sendMessage(format(player, message));
    }

    public void message(CommandSender sender, OfflinePlayer offlinePlayer, String message) {
        if (offlinePlayer instanceof Player) message(sender, (Player) offlinePlayer, message);
        else sender.sendMessage(format(offlinePlayer, message));
    }

    public void message(CommandSender sender, String message) {
        if (sender instanceof Player) message(sender, (Player) sender, message);
        else sender.sendMessage(colorize(message));
    }

    public void console(String message) {
        Bukkit.getConsoleSender().sendMessage(colorize(localPlaceholders(message)));
    }

    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @NotNull
    private String format(Player player, String message) {
        return PlaceholderAPI.setPlaceholders(player, localPlaceholders(player, colorize(message)));
    }

    @NotNull
    private String format(OfflinePlayer offlinePlayer, String message) {
        return PlaceholderAPI.setPlaceholders(offlinePlayer, localPlaceholders(offlinePlayer, colorize(message)));
    }

    private String localPlaceholders(String message) {
        return message.replace("%prefix%", this.plugin.getConfig().getString(ConfigKeys.PREFIX));
    }

    private String localPlaceholders(Player player, String message) {
        return message.replace("%prefix%", this.plugin.getConfig().getString(ConfigKeys.PREFIX));
    }

    private String localPlaceholders(OfflinePlayer offlinePlayer, String message) {
        return message.replace("%prefix%", this.plugin.getConfig().getString(ConfigKeys.PREFIX));
    }
}
