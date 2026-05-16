package de.yolacraft.lcqbot.model;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private String id;
    private String name;
    private String apiKey;
    private EventStatus status;
    private long createdAt;

    // wird per /config gesetzt
    private String adminRoleId;
    private String activePlayerRoleId;
    private String resultsChannelId;
    private String initiatedChannelId;
    private String liveboardChannelId;

    // ID(s) der Leaderboard-Nachricht(en)
    private List<String> leaderboardMessageIds = new ArrayList<>();

    // Aktueller Seed und Fortschritt
    private int currentSeed;

    // Rolle für das Event
    private String roleId;

    public Event() {}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getAdminRoleId() {
        return adminRoleId;
    }

    public void setAdminRoleId(String adminRoleId) {
        this.adminRoleId = adminRoleId;
    }

    public String getActivePlayerRoleId() {
        return activePlayerRoleId;
    }

    public void setActivePlayerRoleId(String activePlayerRoleId) {
        this.activePlayerRoleId = activePlayerRoleId;
    }

    public String getResultsChannelId() {
        return resultsChannelId;
    }

    public void setResultsChannelId(String resultsChannelId) {
        this.resultsChannelId = resultsChannelId;
    }

    public String getLiveboardChannelId() {
        return liveboardChannelId;
    }

    public void setLiveboardChannelId(String liveboardChannelId) {
        this.liveboardChannelId = liveboardChannelId;
    }

    public String getInitiatedChannelId() {
        return initiatedChannelId;
    }

    public void setInitiatedChannelId(String initiatedChannelId) {
        this.initiatedChannelId = initiatedChannelId;
    }

    public List<String> getLeaderboardMessageIds() {
        return leaderboardMessageIds;
    }

    public void setLeaderboardMessageIds(List<String> leaderboardMessageIds) {
        this.leaderboardMessageIds = leaderboardMessageIds != null ? leaderboardMessageIds : new ArrayList<>();
    }

    public String getLeaderboardMessageId() {
        return leaderboardMessageIds.isEmpty() ? null : leaderboardMessageIds.get(0);
    }

    public void setLeaderboardMessageId(String leaderboardMessageId) {
        this.leaderboardMessageIds = new ArrayList<>();
        if (leaderboardMessageId != null) {
            this.leaderboardMessageIds.add(leaderboardMessageId);
        }
    }

    public int getCurrentSeed() {
        return currentSeed;
    }

    public void setCurrentSeed(int currentSeed) {
        this.currentSeed = currentSeed;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}

