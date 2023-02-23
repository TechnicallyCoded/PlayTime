package com.tcoded.playtime.utils;

import java.util.UUID;

public class PlayerPlayTimeData {

    private final UUID uuid;
    private String lastKnownName;
    private long secondsPlayed;
    private int joinCount;
    private long lastJoinTime;
    private long lastQuitTime;

    public PlayerPlayTimeData(UUID uuid, String lastKnownName, long secondsPlayed, int joinCount, long lastJoinTime, long lastQuitTime) {
        this.uuid = uuid;
        this.lastKnownName = lastKnownName;
        this.secondsPlayed = secondsPlayed;
        this.joinCount = joinCount;
        this.lastJoinTime = lastJoinTime;
        this.lastQuitTime = lastQuitTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public long getSecondsPlayed() {
        return secondsPlayed;
    }

    public void setSecondsPlayed(long secondsPlayed) {
        this.secondsPlayed = secondsPlayed;
    }

    public int getJoinCount() {
        return joinCount;
    }

    public void setJoinCount(int joinCount) {
        this.joinCount = joinCount;
    }

    public long getLastJoinTime() {
        return lastJoinTime;
    }

    public void setLastJoinTime(long lastJoinTime) {
        this.lastJoinTime = lastJoinTime;
    }

    public long getLastQuitTime() {
        return lastQuitTime;
    }

    public void setLastQuitTime(long lastQuitTime) {
        this.lastQuitTime = lastQuitTime;
    }

    public static String serialize(PlayerPlayTimeData data) {
        return data.getLastKnownName() + "\n" +
                data.getSecondsPlayed() + "\n" +
                data.getJoinCount() + "\n" +
                data.getLastJoinTime() + "\n" +
                data.getLastQuitTime();
    }

    public static PlayerPlayTimeData deserialize(UUID uuid, String rawData) {
        String[] lines = rawData.split("\n");
        String lastKnownName = lines[0];
        long secondsPlayed = Long.parseLong(lines[1]);
        int joinCount = Integer.parseInt(lines[2]);
        long lastJoinTime = Long.parseLong(lines[3]);
        long lastQuitTime = Long.parseLong(lines[4]);

        return new PlayerPlayTimeData(uuid, lastKnownName, secondsPlayed, joinCount, lastJoinTime, lastQuitTime);
    }
}
