package de.yolacraft.lcqbot.api;

public record PlacementDto(
        String playerId,
        boolean finished,
        int place,
        int points,
        long finishTimeMs
) {
}
