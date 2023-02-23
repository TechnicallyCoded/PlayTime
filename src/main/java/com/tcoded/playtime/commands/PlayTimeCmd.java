package com.tcoded.playtime.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.tcoded.playtime.PlayTime;
import com.tcoded.playtime.utils.ChatUtil;
import com.tcoded.playtime.utils.ConfigKeys;
import com.tcoded.playtime.utils.PlayerPlayTimeData;
import me.f64.playtime.utils.*;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PlayTimeCmd implements TabExecutor {

    private final PlayTime plugin;

    public PlayTimeCmd(PlayTime plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
            FileConfiguration config = this.plugin.getConfig();

        ChatUtil chatUtil = this.plugin.getChatUtil();
        if (!sender.hasPermission("playtime.check")) {
                chatUtil.message(sender, config.getString(ConfigKeys.MESSAGES_NO_PERMISSION));
                return true;
            }

            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    for (String thisPlayer : config.getStringList(ConfigKeys.MESSAGES_PLAYER_INFO)) {
                        chatUtil.message(sender, player, thisPlayer);
                    }
                }
                else {
                    chatUtil.message(sender, "&c/playtime <username>");
                }
            }
            // There are args
            else {
                String arg0 = args[0].toLowerCase();

                if (arg0.equals("reload")) {
                    if (!checkPermission(sender, "playtime.command.playtime.reload")) return true;
                    chatUtil.message(sender, config.getString("messages.reload_config"));
                    this.plugin.reloadConfig();
                }
                else if (arg0.equals("uptime")) {
                    if (!checkPermission(sender, "playtime.command.playtime.uptime")) return true;
                    chatUtil.message(sender, ConfigKeys.MESSAGES_SERVER_UPTIME);
                }
                else if (arg0.equals("top")) {
                    chatUtil.message(sender, "&cTODO - Not implemented");
                }
                else {
                    Player target = plugin.getServer().getPlayer(arg0);

                    if (target == null) {
                        OfflinePlayer offlineTarget = plugin.getServer().getOfflinePlayer(arg0);
                        UUID offlineTargetId = offlineTarget.getUniqueId();

                        CompletableFuture<Optional<PlayerPlayTimeData>> future =
                                this.plugin.getPlayerDataManager().findOfflinePlayerData(offlineTargetId);

                        future.thenAccept(offlinePlayerData -> {
                            if (offlinePlayerData.isPresent()) {
                                this.sendOtherPlayerStats(sender, offlineTarget, offlinePlayerData.get());
                            } else {
                                chatUtil.message(sender, config.getString(ConfigKeys.MESSAGES_DOESNT_EXIST));
                            }
                        });
                    }
                    else {
                        UUID targetId = target.getUniqueId();
                        PlayerPlayTimeData data = this.plugin.getPlayerDataManager().getOrLoadData(targetId);
                        if (data != null) this.sendOtherPlayerStats(sender, target, data);
                        else chatUtil.message(sender, config.getString(ConfigKeys.MESSAGES_INTERNAL_ERROR));
                    }
                }
            }
        return true;
    }

    private void sendOtherPlayerStats(CommandSender sender, OfflinePlayer target, PlayerPlayTimeData data) {
        FileConfiguration config = this.plugin.getConfig();

        for (String messagePart : config.getStringList(ConfigKeys.MESSAGES_OTHER_PLAYER_INFO)) {
            this.plugin.getChatUtil().message(sender, target, messagePart);
        }
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;

        this.plugin.getChatUtil().message(sender, ConfigKeys.MESSAGES_NO_PERMISSION);
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> tabComplete = new ArrayList<>();

        String[] options = {"reload", "uptime", "top"};

        String lowerArg0 = args[0].toLowerCase();
        for (String option : options) {
            if (lowerArg0.startsWith(option.toLowerCase())) tabComplete.add(option);
        }

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            String pName = p.getName();
            if (lowerArg0.startsWith(pName.toLowerCase())) tabComplete.add(pName);
        }


        return null;
    }
}