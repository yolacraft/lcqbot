package de.yolacraft.lcqbot.api;

import de.yolacraft.lcqbot.model.api.EventStatus;

public record EventSummaryDto(
        String id,
        String name,
        EventStatus status,
        long createdAt,
        int currentSeed,
        String adminRoleId,
        String activePlayerRoleId,
        String resultsChannelId,
        String initiatedChannelId,
        String liveboardChannelId,
        String leaderboardMessageId,
        String roleId
) {
}
