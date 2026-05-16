package de.yolacraft.lcqbot.seasonleaderboard;

public class SeasonLeaderboardException extends RuntimeException {

    public SeasonLeaderboardException(String message) {
        super(message);
    }

    public SeasonLeaderboardException(String message, Throwable cause) {
        super(message, cause);
    }
}
