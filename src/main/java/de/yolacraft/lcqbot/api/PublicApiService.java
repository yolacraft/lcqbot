package de.yolacraft.lcqbot.api;

import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.EventStatus;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.model.Seed;
import de.yolacraft.lcqbot.model.SeedStatus;
import de.yolacraft.lcqbot.model.api.McsrResponse;
import de.yolacraft.lcqbot.service.MatchTrackingService;
import de.yolacraft.lcqbot.storage.FileStorageService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PublicApiService {

    private final FileStorageService storage;
    private final MatchTrackingService trackingService;

    public PublicApiService(FileStorageService storage, MatchTrackingService trackingService) {
        this.storage = storage;
        this.trackingService = trackingService;
    }

    public List<EventSummaryDto> listEvents() {
        return storage.loadAllEvents().stream().map(this::toSummaryDto).toList();
    }

    public EventDetailDto getLatestEvent() {
        Event event = storage.findLatestEvent()
                .orElseThrow(() -> new NoSuchElementException("No active event found"));
        return getEventDetail(event.getId());
    }

    public String getLatestLiveData() {
        if (!trackingService.isTracking()) {
            throw new NoSuchElementException("Kein aktives Tracking");
        }
        return trackingService.getLatestLiveDataRaw();
    }

    public EventDetailDto getEventDetail(String eventId) {
        Event event = storage.findEventById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));
        List<Player> players = storage.loadPlayers(eventId);
        List<Seed> seeds = storage.loadSeeds(eventId);
        List<LeaderboardEntryDto> leaderboard = calculateLeaderboard(players, seeds);
        return new EventDetailDto(
                toSummaryDto(event),
                players.stream().map(this::toPlayerDto).toList(),
                seeds.stream().map(this::toSeedDto).toList(),
                leaderboard
        );
    }

    public List<PlayerDto> listPlayers(String eventId) {
        validateEventExists(eventId);
        return storage.loadPlayers(eventId).stream().map(this::toPlayerDto).toList();
    }

    public PlayerDto getPlayer(String eventId, String playerId) {
        validateEventExists(eventId);
        return storage.loadPlayers(eventId).stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .map(this::toPlayerDto)
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + playerId));
    }

    public List<SeedDto> listSeeds(String eventId) {
        validateEventExists(eventId);
        return storage.loadSeeds(eventId).stream().map(this::toSeedDto).toList();
    }

    public SeedDto getSeed(String eventId, int seedNumber) {
        validateEventExists(eventId);
        return storage.loadSeeds(eventId).stream()
                .filter(seed -> seed.getSeedNumber() == seedNumber)
                .findFirst()
                .map(this::toSeedDto)
                .orElseThrow(() -> new NoSuchElementException("Seed not found: " + seedNumber));
    }

    public List<LeaderboardEntryDto> getLeaderboard(String eventId) {
        validateEventExists(eventId);
        List<Player> players = storage.loadPlayers(eventId);
        List<Seed> seeds = storage.loadSeeds(eventId);
        return calculateLeaderboard(players, seeds);
    }

    private void validateEventExists(String eventId) {
        if (storage.findEventById(eventId).isEmpty()) {
            throw new NoSuchElementException("Event not found: " + eventId);
        }
    }

    private EventSummaryDto toSummaryDto(Event event) {
        return new EventSummaryDto(
                event.getId(),
                event.getName(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getCurrentSeed(),
                event.getAdminRoleId(),
                event.getActivePlayerRoleId(),
                event.getResultsChannelId(),
                event.getInitiatedChannelId(),
                event.getLiveboardChannelId(),
                event.getLeaderboardMessageId(),
                event.getRoleId()
        );
    }

    private PlayerDto toPlayerDto(Player player) {
        return new PlayerDto(
                player.getId(),
                player.getEventId(),
                player.getIngameName(),
                player.getDiscordName(),
                player.getAlias(),
                player.isEliminated()
        );
    }

    private SeedDto toSeedDto(Seed seed) {
        return new SeedDto(
                seed.getId(),
                seed.getEventId(),
                seed.getSeedNumber(),
                seed.getStatus(),
                seed.getStartedAt(),
                seed.getConfirmedAt(),
                seed.getPlacements().stream().map(this::toPlacementDto).toList()
        );
    }

    private PlacementDto toPlacementDto(de.yolacraft.lcqbot.model.Placement placement) {
        return new PlacementDto(
                placement.getPlayerId(),
                placement.isFinished(),
                placement.getPlace(),
                placement.getPoints(),
                placement.getFinishTimeMs()
        );
    }

    private List<LeaderboardEntryDto> calculateLeaderboard(List<Player> players, List<Seed> seeds) {
        Map<String, Integer> pointsByPlayer = initializePoints(players);
        seeds.stream()
                .filter(seed -> seed.getStatus() == SeedStatus.CONFIRMED)
                .flatMap(seed -> seed.getPlacements().stream())
                .forEach(placement -> pointsByPlayer.merge(placement.getPlayerId(), placement.getPoints(), Integer::sum));

        return players.stream()
                .map(player -> new LeaderboardEntryDto(
                        player.getId(),
                        player.getIngameName(),
                        player.getAlias(),
                        pointsByPlayer.getOrDefault(player.getId(), 0),
                        player.isEliminated()
                ))
                .sorted((a, b) -> Integer.compare(b.points(), a.points()))
                .toList();
    }

    private Map<String, Integer> initializePoints(List<Player> players) {
        Map<String, Integer> pointsByPlayer = new HashMap<>();
        for (Player player : players) {
            pointsByPlayer.put(player.getId(), 0);
        }
        return pointsByPlayer;
    }
}
