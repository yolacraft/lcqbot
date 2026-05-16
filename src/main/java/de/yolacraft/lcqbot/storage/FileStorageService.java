package de.yolacraft.lcqbot.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.EventStatus;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.model.ReactionRole;
import de.yolacraft.lcqbot.model.Seed;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
@Service
public class FileStorageService {

    private static final boolean DEBUG = false;

    private static final String DATA_DIR = "data/";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FileStorageService() {
        new File(DATA_DIR).mkdirs();
        log("Storage initialized at {}", DATA_DIR);
    }

    // ── DEBUG HELPER ─────────────────────────────────────────────

    private void log(String msg, Object... args) {
        if (!DEBUG) return;
        String formatted = (args.length > 0)
                ? String.format(msg.replace("{}", "%s"), args)
                : msg;
        System.out.println("[FileStorageService] " + formatted);
    }

    // ── Events ────────────────────────────────────────────────────

    private File eventsFile() {
        File f = new File(DATA_DIR + "events.json");
        log("eventsFile() -> {}", f.getPath());
        return f;
    }

    public List<Event> loadAllEvents() {
        log("Loading all events");
        return loadList(eventsFile(), Event.class);
    }

    public void saveEvent(Event event) {
        log("Saving event id={}", event.getId());

        List<Event> events = loadAllEvents();
        int before = events.size();

        events.removeIf(e -> e.getId().equals(event.getId()));
        events.add(event);

        log("Events updated: {} -> {}", before, events.size());

        writeAtomically(eventsFile(), events);
    }

    public Optional<Event> findLatestEvent() {
        log("Finding latest active event");

        return loadAllEvents().stream()
                .filter(e -> e.getStatus() != EventStatus.FINISHED)
                .max(Comparator.comparingLong(Event::getCreatedAt));
    }

    public Optional<Event> findEventById(String eventId) {
        log("Finding event by id={}", eventId);

        return loadAllEvents().stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst();
    }

    // ── Players ───────────────────────────────────────────────────

    private File playersFile(String eventId) {
        File f = new File(DATA_DIR + "players_" + eventId + ".json");
        log("playersFile(eventId={}) -> {}", eventId, f.getPath());
        return f;
    }

    public List<Player> loadPlayers(String eventId) {
        log("Loading players for eventId={}", eventId);
        return loadList(playersFile(eventId), Player.class);
    }

    public void savePlayer(Player player) {
        log("Saving player id={} eventId={}", player.getId(), player.getEventId());

        List<Player> players = loadPlayers(player.getEventId());
        int before = players.size();

        players.removeIf(p -> p.getId().equals(player.getId()));
        players.add(player);

        log("Players updated: {} -> {}", before, players.size());

        writeAtomically(playersFile(player.getEventId()), players);
    }

    public void deletePlayer(String eventId, String discordUserId) {
        log("Deleting player discordUserId={} eventId={}", discordUserId, eventId);

        List<Player> players = loadPlayers(eventId);

        boolean removed = players.removeIf(p -> p.getDiscordUserId().equals(discordUserId));

        log("Player removed? {}", removed);

        if (removed) {
            writeAtomically(playersFile(eventId), players);
        }
    }

    // ── Seeds ─────────────────────────────────────────────────────

    private File seedsFile(String eventId) {
        File f = new File(DATA_DIR + "seeds_" + eventId + ".json");
        log("seedsFile(eventId={}) -> {}", eventId, f.getPath());
        return f;
    }

    public List<Seed> loadSeeds(String eventId) {
        log("Loading seeds for eventId={}", eventId);
        return loadList(seedsFile(eventId), Seed.class);
    }

    public void saveSeed(Seed seed) {
        log("Saving seed id={} eventId={}", seed.getId(), seed.getEventId());

        List<Seed> seeds = loadSeeds(seed.getEventId());
        int before = seeds.size();

        seeds.removeIf(s -> s.getId().equals(seed.getId()));
        seeds.add(seed);

        log("Seeds updated: {} -> {}", before, seeds.size());

        writeAtomically(seedsFile(seed.getEventId()), seeds);
    }

    // ── Reaction Roles ───────────────────────────────────────────

    private File reactionRolesFile() {
        File f = new File(DATA_DIR + "reaction_roles.json");
        log("reactionRolesFile() -> {}", f.getPath());
        return f;
    }

    public List<ReactionRole> loadReactionRoles() {
        log("Loading reaction roles");
        return loadList(reactionRolesFile(), ReactionRole.class);
    }

    public Optional<ReactionRole> findReactionRoleByMessageId(String messageId) {
        log("Finding reaction role by messageId={}", messageId);

        return loadReactionRoles().stream()
                .filter(entry -> entry.getMessageId() != null && entry.getMessageId().equals(messageId))
                .findFirst();
    }

    public void saveReactionRole(ReactionRole reactionRole) {
        log("Saving reaction role messageId={}", reactionRole.getMessageId());

        List<ReactionRole> roles = loadReactionRoles();
        int before = roles.size();

        roles.removeIf(entry ->
                entry.getMessageId() != null &&
                        entry.getMessageId().equals(reactionRole.getMessageId()));

        roles.add(reactionRole);

        log("ReactionRoles updated: {} -> {}", before, roles.size());

        writeAtomically(reactionRolesFile(), roles);
    }

    public void deleteReactionRole(String messageId) {
        log("Deleting reaction role messageId={}", messageId);

        List<ReactionRole> roles = loadReactionRoles();

        boolean removed = roles.removeIf(entry ->
                entry.getMessageId() != null &&
                        entry.getMessageId().equals(messageId));

        log("ReactionRole removed? {}", removed);

        if (removed) {
            writeAtomically(reactionRolesFile(), roles);
        }
    }

    // ── GENERIC IO ───────────────────────────────────────────────

    private <T> List<T> loadList(File file, Class<T> clazz) {
        log("loadList file={}", file.getName());

        if (!file.exists()) {
            log("File does not exist -> returning empty list");
            return new ArrayList<>();
        }

        try (Reader r = new FileReader(file)) {
            Type listType = TypeToken.getParameterized(List.class, clazz).getType();
            List<T> result = gson.fromJson(r, listType);

            log("Loaded {} entries from {}", result != null ? result.size() : 0, file.getName());

            return result != null ? result : new ArrayList<>();
        } catch (IOException e) {
            log("ERROR reading file {}", file.getName());
            throw new RuntimeException("Fehler beim Lesen: " + file.getName(), e);
        }
    }

    private void writeAtomically(File target, Object data) {
        log("writeAtomically -> {}", target.getName());

        File tmp = new File(target.getPath() + ".tmp");

        try (Writer w = new FileWriter(tmp)) {
            gson.toJson(data, w);
        } catch (IOException e) {
            log("ERROR writing temp file {}", tmp.getName());
            throw new RuntimeException("Fehler beim Schreiben: " + target.getName(), e);
        }

        if (!tmp.renameTo(target)) {
            log("rename failed -> fallback delete+rename");
            target.delete();
            tmp.renameTo(target);
        }

        log("Write successful -> {}", target.getName());
    }
}