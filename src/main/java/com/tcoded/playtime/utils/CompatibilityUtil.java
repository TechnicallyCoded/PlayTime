package com.tcoded.playtime.utils;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;

public final class CompatibilityUtil {

    private static Boolean IS_LEGACY = null;
    private static Statistic PLAY_ONE_TICK_STAT = null;

    public static boolean isLegacyServer() {
        if (IS_LEGACY != null) return IS_LEGACY;

        String featureVersionPart = Bukkit.getVersion().split("\\.")[1];
        int featureVersion = Integer.parseInt(featureVersionPart);
        IS_LEGACY = featureVersion < 13;

        return IS_LEGACY;
    }

    public static Statistic getTicksPlayedStatEnum() {
        if (PLAY_ONE_TICK_STAT != null) return PLAY_ONE_TICK_STAT;

        try {
            if (isLegacyServer()) PLAY_ONE_TICK_STAT = Statistic.valueOf("PLAY_ONE_TICK");
            else PLAY_ONE_TICK_STAT = Statistic.valueOf("PLAY_ONE_MINUTE");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        return PLAY_ONE_TICK_STAT;
    }
}
