package de.yolacraft.lcqbot.api;

public record PlayerDto(
        String id,
        String eventId,
        String ingameName,
        String discordName,
        String alias,
        boolean eliminated
) {
}
