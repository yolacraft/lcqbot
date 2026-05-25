package de.yolacraft.lcqbot.api;

import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.model.Seed;
import de.yolacraft.lcqbot.service.LeaderboardService;
import de.yolacraft.lcqbot.storage.FileStorageService;
import de.yolacraft.lcqbot.model.api.SeedStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PublicApiV2Service {

    private final FileStorageService storage;
    private final LeaderboardService leaderboardService;
    private final HttpClient httpClient;

    public PublicApiV2Service(FileStorageService storage, LeaderboardService leaderboardService) {
        this.storage = storage;
        this.leaderboardService = leaderboardService;
        this.httpClient = HttpClient.newHttpClient();
    }

    public EventDetailDto getLatestEvent() {
        Event event = storage.getEvent(null);
        String eventId = event.getUuid().toString();

        List<Player> players = storage.getPlayers(eventId);
        List<Seed> seeds = storage.loadSeeds(eventId);
        List<LeaderboardEntryDto> leaderboard = leaderboardService.getLeaderboard(eventId);

        return new EventDetailDto(
                toSummaryDto(event, seeds),
                players.stream().map(this::toPlayerDto).toList(),
                seeds.stream().map(seed -> toSeedDto(seed, eventId, players)).toList(),
                leaderboard
        );
    }

    private EventSummaryDto toSummaryDto(Event event, List<Seed> seeds) {
        String leaderboardMessageId = null;
        if (event.getLeaderboardMessageIds() != null && !event.getLeaderboardMessageIds().isEmpty()) {
            leaderboardMessageId = event.getLeaderboardMessageIds().get(0);
        }

        return new EventSummaryDto(
                event.getUuid().toString(),
                event.getName(),
                null,
                event.getCreationTime(),
                seeds.size(),
                null,
                event.getPlayerRoleId(),
                event.getResultChannelId(),
                event.getBotCommandChannelId(),
                event.getLeaderboardChannelId(),
                leaderboardMessageId,
                event.getPlayerRoleId()
        );
    }

    private PlayerDto toPlayerDto(Player player) {
        return new PlayerDto(
                player.getUuid().toString(),
                null,
                player.getIgn(),
                player.getDiscordUserId(),
                player.getTwitchUserName(),
                player.isEliminated()
        );
    }

    private SeedDto toSeedDto(Seed seed, String eventId, List<Player> players) {
        SeedStatus status = seed.getCompletions() == null || seed.getCompletions().isEmpty()
                ? SeedStatus.PENDING
                : SeedStatus.CONFIRMED;

        return new SeedDto(
                seed.getUuid().toString(),
                eventId,
                seed.getSeedNumber(),
                status,
                seed.getStartedAt(),
                status == SeedStatus.CONFIRMED ? seed.getStartedAt() : 0,
                toPlacementDtos(seed, players)
        );
    }

    private List<PlacementDto> toPlacementDtos(Seed seed, List<Player> players) {
        if (seed.getCompletions() == null || seed.getCompletions().isEmpty()) {
            return List.of();
        }

        Map<UUID, String> minecraftToPlayerUuid = players.stream()
                .map(player -> Map.entry(tryParseUuid(player.getMinecraftUUID()), player.getUuid().toString()))
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Map.Entry<UUID, Long>> sorted = new ArrayList<>(seed.getCompletions().entrySet());
        sorted.sort(Comparator.comparingLong(Map.Entry::getValue));

        List<PlacementDto> placements = new ArrayList<>();
        int points = 12;

        for (int index = 0; index < sorted.size(); index++) {
            Map.Entry<UUID, Long> entry = sorted.get(index);
            String playerId = minecraftToPlayerUuid.getOrDefault(entry.getKey(), entry.getKey().toString());
            long finishTime = entry.getValue();

            placements.add(new PlacementDto(
                    playerId,
                    true,
                    index + 1,
                    Math.max(points, 1),
                    finishTime
            ));

            if (points > 1) {
                points--;
            }
        }

        return placements;
    }

    public String getLatestLiveData() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mcsrranked.com/users/razekatze/live"))
                    .header("Private-key", "93cu35h0fd9yfrql09qto9ce5aicpn1chc5t5snm1wi2x0xws5v1cp7awluvuq9p")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new NoSuchElementException("Failed to fetch live data: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new NoSuchElementException("Error fetching live data: " + e.getMessage());
        }
    }

    private UUID tryParseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            String cleaned = value.replaceAll("[-\\s]", "");
            if (cleaned.length() == 32) {
                try {
                    return UUID.fromString(cleaned.substring(0, 8) + "-" + cleaned.substring(8, 12) + "-" + cleaned.substring(12, 16) + "-" + cleaned.substring(16, 20) + "-" + cleaned.substring(20));
                } catch (IllegalArgumentException ignored2) {
                    return null;
                }
            }
            return null;
        }
    }
}
