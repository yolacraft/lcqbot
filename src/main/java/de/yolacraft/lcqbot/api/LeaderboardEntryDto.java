package de.yolacraft.lcqbot.api;

public record LeaderboardEntryDto(
        String playerId,
        String ingameName,
        String alias,
        int points,
        boolean eliminated
) {
}
