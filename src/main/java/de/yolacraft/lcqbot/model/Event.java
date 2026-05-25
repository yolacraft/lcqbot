package de.yolacraft.lcqbot.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Event {
    private UUID uuid;
    private String name;

    private String playerRoleId;

    private String botCommandChannelId;
    private String leaderboardChannelId;
    private String logChannelId;
    private String resultChannelId;
    private List<String> leaderboardMessageIds = new ArrayList<>();

    private long creationTime;

    public Event(String name, String playerRoleId, String botCommandChannelId, String leaderboardChannelId, String logChannelId, String resultChannelId) {
        this.name = name;
        this.playerRoleId = playerRoleId;
        this.botCommandChannelId = botCommandChannelId;
        this.leaderboardChannelId = leaderboardChannelId;
        this.logChannelId = logChannelId;
        this.resultChannelId = resultChannelId;
        this.uuid = UUID.randomUUID();
        creationTime = System.currentTimeMillis();
        leaderboardMessageIds = null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlayerRoleId() {
        return playerRoleId;
    }

    public void setPlayerRoleId(String playerRoleId) {
        this.playerRoleId = playerRoleId;
    }

    public String getBotCommandChannelId() {
        return botCommandChannelId;
    }

    public void setBotCommandChannelId(String botCommandChannelId) {
        this.botCommandChannelId = botCommandChannelId;
    }

    public String getLeaderboardChannelId() {
        return leaderboardChannelId;
    }

    public void setLeaderboardChannelId(String leaderboardChannelId) {
        this.leaderboardChannelId = leaderboardChannelId;
    }

    public String getLogChannelId() {
        return logChannelId;
    }

    public void setLogChannelId(String logChannelId) {
        this.logChannelId = logChannelId;
    }

    public String getResultChannelId() {
        return resultChannelId;
    }

    public void setResultChannelId(String resultChannelId) {
        this.resultChannelId = resultChannelId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public List<String> getLeaderboardMessageIds() {
        return leaderboardMessageIds;
    }

    public void setLeaderboardMessageIds(List<String> leaderboardMessageIds) {
        this.leaderboardMessageIds = leaderboardMessageIds;
    }
}
