package de.yolacraft.lcqbot.api;

import java.util.List;

public record EventDetailDto(
        EventSummaryDto event,
        List<PlayerDto> players,
        List<SeedDto> seeds,
        List<LeaderboardEntryDto> leaderboard
) {
}
