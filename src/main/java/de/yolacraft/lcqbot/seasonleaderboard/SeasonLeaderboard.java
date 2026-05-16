package de.yolacraft.lcqbot.seasonleaderboard;

import java.util.LinkedHashMap;
import java.util.Map;

public class SeasonLeaderboard {

    private String id;
    private int season;
    private int week;
    private String channelId;
    private String messageId;
    private long createdAt;
    private Map<String, SeasonLeaderboardEntry> players = new LinkedHashMap<>();

    public SeasonLeaderboard() {
        this.players = new LinkedHashMap<>();
    }

    public SeasonLeaderboard(String id, int season, int week, String channelId, String messageId, long createdAt) {
        this.id = id;
        this.season = season;
        this.week = week;
        this.channelId = channelId;
        this.messageId = messageId;
        this.createdAt = createdAt;
        this.players = new LinkedHashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, SeasonLeaderboardEntry> getPlayers() {
        if (players == null) {
            players = new LinkedHashMap<>();
        }
        return players;
    }

    public void setPlayers(Map<String, SeasonLeaderboardEntry> players) {
        this.players = players != null ? players : new LinkedHashMap<>();
    }

    public void normalizePlayers() {
        if (players == null) {
            players = new LinkedHashMap<>();
            return;
        }
        players.values().forEach(entry -> {
            if (entry.getWeeks() == null || entry.getWeeks().length != 4) {
                entry.setWeeks(new int[4]);
            }
            entry.recalculateTotal();
        });
    }
}
