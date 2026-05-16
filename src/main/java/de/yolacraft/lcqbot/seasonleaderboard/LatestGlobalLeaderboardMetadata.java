package de.yolacraft.lcqbot.seasonleaderboard;

public class LatestGlobalLeaderboardMetadata {

    private String latestLeaderboardId;

    public LatestGlobalLeaderboardMetadata() {
    }

    public LatestGlobalLeaderboardMetadata(String latestLeaderboardId) {
        this.latestLeaderboardId = latestLeaderboardId;
    }

    public String getLatestLeaderboardId() {
        return latestLeaderboardId;
    }

    public void setLatestLeaderboardId(String latestLeaderboardId) {
        this.latestLeaderboardId = latestLeaderboardId;
    }
}
