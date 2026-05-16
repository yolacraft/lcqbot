package de.yolacraft.lcqbot.api;

import de.yolacraft.lcqbot.model.SeedStatus;
import java.util.List;

public record SeedDto(
        String id,
        String eventId,
        int seedNumber,
        SeedStatus status,
        long startedAt,
        long confirmedAt,
        List<PlacementDto> placements
) {
}
