package de.yolacraft.lcqbot.api;

import de.yolacraft.lcqbot.model.api.McsrResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public")
public class PublicApiController {

    private final PublicApiService publicApiService;

    public PublicApiController(PublicApiService publicApiService) {
        this.publicApiService = publicApiService;
    }

    @GetMapping("/events")
    public List<EventSummaryDto> listEvents() {
        System.out.println("/events");
        return publicApiService.listEvents();
    }

    @GetMapping("/events/latest")
    public EventDetailDto getLatestEvent() {
        System.out.println("/events/latest");
        try {
            return publicApiService.getLatestEvent();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/events/latest/live")
    public ResponseEntity<String> getLiveData() {
        try {
            String raw = publicApiService.getLatestLiveData();
            if (raw == null) {
                return ResponseEntity.noContent().build(); // 204, noch kein Poll
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(raw);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/events/{eventId}")
    public EventDetailDto getEventDetail(@PathVariable String eventId) {
        System.out.println("/events/id");
        try {
            return publicApiService.getEventDetail(eventId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/events/{eventId}/players")
    public List<PlayerDto> listPlayers(@PathVariable String eventId) {
        try {
            return publicApiService.listPlayers(eventId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/events/{eventId}/players/{playerId}")
    public PlayerDto getPlayer(@PathVariable String eventId, @PathVariable String playerId) {
        try {
            return publicApiService.getPlayer(eventId, playerId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/events/{eventId}/seeds")
    public List<SeedDto> listSeeds(@PathVariable String eventId) {
        try {
            return publicApiService.listSeeds(eventId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/events/{eventId}/seeds/{seedNumber}")
    public SeedDto getSeed(@PathVariable String eventId, @PathVariable int seedNumber) {
        try {
            return publicApiService.getSeed(eventId, seedNumber);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/events/{eventId}/leaderboard")
    public List<LeaderboardEntryDto> getLeaderboard(@PathVariable String eventId) {
        try {
            return publicApiService.getLeaderboard(eventId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}
