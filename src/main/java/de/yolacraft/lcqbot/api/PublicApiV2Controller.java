package de.yolacraft.lcqbot.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import de.yolacraft.lcqbot.api.PublicApiV2Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v2/events")
public class PublicApiV2Controller {

    private final PublicApiV2Service publicApiV2Service;

    public PublicApiV2Controller(PublicApiV2Service publicApiV2Service) {
        this.publicApiV2Service = publicApiV2Service;
    }

    @GetMapping("/latest")
    public EventDetailDto getLatestEvent() {
        try {
            return publicApiV2Service.getLatestEvent();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/latest/live")
    public ResponseEntity<String> getLatestLiveData() {
        try {
            String raw = publicApiV2Service.getLatestLiveData();
            if (raw == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(raw);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
