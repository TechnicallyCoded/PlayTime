package com.tcoded.playtime.utils;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.tcoded.playtime.PlayTime;
import org.bukkit.configuration.file.FileConfiguration;

public class TimeFormat {

    private PlayTime plugin;
    private final int oneWeek = 7 * 24 * 60 * 60;
    private final int oneDay = 24 * 60 * 60;
    private final int oneHour = 60 * 60;
    private final int oneMinute = 60;

    public TimeFormat(PlayTime plugin) {
        this.plugin = plugin;
    }

    public String formatTime(Duration duration) {
        // Get info from config for what to display
        FileConfiguration config = plugin.getConfig();
        String secSuffix = config.getString(ConfigKeys.TIME_SECOND_SUFFIX, "s");
        String minSuffix = config.getString(ConfigKeys.TIME_MINUTE_SUFFIX, "m");
        String hourSuffix = config.getString(ConfigKeys.TIME_HOUR_SUFFIX, "h");
        String daySuffix = config.getString(ConfigKeys.TIME_DAY_SUFFIX, "d");
        String weekSuffix = config.getString(ConfigKeys.TIME_WEEK_SUFFIX, "w");

        boolean secEnabled = config.getBoolean(ConfigKeys.TIME_SECOND_ENABLED, true);
        boolean minEnabled = config.getBoolean(ConfigKeys.TIME_MINUTE_ENABLED, true);
        boolean hourEnabled = config.getBoolean(ConfigKeys.TIME_HOUR_ENABLED, true);
        boolean dayEnabled = config.getBoolean(ConfigKeys.TIME_DAY_ENABLED, true);
        boolean weekEnabled = config.getBoolean(ConfigKeys.TIME_WEEK_ENABLED, true);

        // Start building the final result
        StringBuilder strb = new StringBuilder();

        // Get the total time in all units
        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        long weeks = duration.toDays() / 7;

        // For each enabled time unit, subtract it from the smaller ones
        // Then, append the amount of such time unit to the final result
        if (weekEnabled) {
            Duration weeksDuration = Duration.of(weeks * 7, ChronoUnit.DAYS);
            seconds -= weeksDuration.getSeconds();
            minutes -= weeksDuration.toMinutes();
            hours -= weeksDuration.toHours();
            days -= weeksDuration.toDays();

            if (weeks > 0) {
                strb.append(weeks);
                strb.append(weekSuffix);
                strb.append(' ');
            }
        }
        if (dayEnabled) {
            Duration daysDuration = Duration.of(days, ChronoUnit.DAYS);
            seconds -= daysDuration.getSeconds();
            minutes -= daysDuration.toMinutes();
            hours -= daysDuration.toHours();

            if (days > 0) {
                strb.append(days);
                strb.append(daySuffix);
                strb.append(' ');
            }
        }
        if (hourEnabled) {
            Duration hoursDuration = Duration.of(hours, ChronoUnit.HOURS);
            seconds -= hoursDuration.getSeconds();
            minutes -= hoursDuration.toMinutes();

            if (hours > 0) {
                strb.append(hours);
                strb.append(hourSuffix);
                strb.append(' ');
            }
        }
        if (minEnabled) {
            Duration minutesDuration = Duration.of(minutes, ChronoUnit.MINUTES);
            seconds -= minutesDuration.getSeconds();

            if (minutes > 0) {
                strb.append(minutes);
                strb.append(minSuffix);
                strb.append(' ');
            }
        }
        if (secEnabled) {
            strb.append(seconds);
            strb.append(secSuffix);
            strb.append(' ');
        }

        // Remove the last space
        int strbLen = strb.length();
        if (strbLen > 0) strb.delete(strbLen - 1, strbLen);

        return strb.toString();
    }

    public String getUptime() {
        return this.formatTime(Duration.of(ManagementFactory.getRuntimeMXBean().getUptime(), ChronoUnit.MILLIS));
    }
}
