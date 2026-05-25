package de.yolacraft.lcqbot.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.model.Seed;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class FileStorageService {

    private static final String DATA_DIR = "data/v2/";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FileStorageService() {
        File directory = new File(DATA_DIR);

        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Created Data Folder: " + DATA_DIR);
        } else {
            System.out.println("Found Data Folder: " + DATA_DIR);
        }
    }

    public void saveEvent(Event event) {
        UUID uuid = event.getUuid();

        File eventFolder = new File(DATA_DIR + uuid);

        if (!eventFolder.exists()) {
            eventFolder.mkdirs();
        }

        File eventFile = new File(eventFolder, "event.json");

        try (FileWriter writer = new FileWriter(eventFile)) {
            gson.toJson(event, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save event.json for UUID: " + uuid, e);
        }
    }

    public Event getEvent(String name) {
        UUID eventUuid = resolveEventUuid(name);

        File eventFile = new File(DATA_DIR + eventUuid + "/event.json");

        if (!eventFile.exists()) {
            throw new IllegalStateException("Event file not found for UUID: " + eventUuid);
        }

        try (FileReader reader = new FileReader(eventFile)) {
            return gson.fromJson(reader, Event.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not read event.json", e);
        }
    }

    public void createNewEvent(String name, String playerRole, String initChannel, String leaderboardChannel, String logChannel, String resultChannel) {
        Event eventData = new Event(name, playerRole, initChannel, leaderboardChannel, logChannel, resultChannel);
        UUID uuid = eventData.getUuid();

        File eventFolder = new File(DATA_DIR + uuid);
        if (!eventFolder.exists()) {
            eventFolder.mkdirs();
        }

        File eventFile = new File(eventFolder, "event.json");

        try (FileWriter writer = new FileWriter(eventFile)) {
            gson.toJson(eventData, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save event.json", e);
        }
    }

    public void addPlayer(Player player, String eventName, String label) {
        UUID eventUuid = resolveEventUuid(eventName);

        File eventFolder = new File(DATA_DIR + eventUuid);
        File playerFolder = new File(eventFolder, "players");

        if (!playerFolder.exists()) {
            playerFolder.mkdirs();
        }

        File playerFile = new File(playerFolder, label + "-" + player.getUuid() + ".json");

        try (FileWriter writer = new FileWriter(playerFile)) {
            gson.toJson(player, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save player file", e);
        }
    }

    public List<Player> getPlayers(String eventName) {
        UUID eventUuid = resolveEventUuid(eventName);

        File playerFolder = new File(DATA_DIR + eventUuid + "/players");

        if (!playerFolder.exists() || !playerFolder.isDirectory()) {
            return List.of();
        }

        File[] playerFiles = playerFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (playerFiles == null || playerFiles.length == 0) {
            return List.of();
        }

        List<Player> players = new ArrayList<>();

        for (File playerFile : playerFiles) {
            try (FileReader reader = new FileReader(playerFile)) {
                Player player = gson.fromJson(reader, Player.class);
                players.add(player);
            } catch (IOException e) {
                System.err.println("Could not read player file: " + playerFile.getName());
            }
        }

        return players;
    }

    public boolean removePlayer(String eventName, String discordUserId) {
        UUID eventUuid = resolveEventUuid(eventName);
        File playerFolder = new File(DATA_DIR + eventUuid + "/players");

        if (!playerFolder.exists() || !playerFolder.isDirectory()) {
            return false;
        }

        File[] playerFiles = playerFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (playerFiles == null || playerFiles.length == 0) {
            return false;
        }

        boolean removed = false;

        for (File playerFile : playerFiles) {
            try (FileReader reader = new FileReader(playerFile)) {
                Player player = gson.fromJson(reader, Player.class);
                if (player != null && discordUserId.equals(player.getDiscordUserId())) {
                    if (!playerFile.delete()) {
                        throw new RuntimeException("Could not delete player file: " + playerFile.getName());
                    }
                    removed = true;
                    break;
                }
            } catch (IOException e) {
                System.err.println("Could not read player file: " + playerFile.getName());
            }
        }

        return removed;
    }

    private UUID resolveEventUuid(String input) {

        if (input == null) {
            return getLatestEventUuid();
        }

        UUID parsedUuid = tryParseUuid(input);
        if (parsedUuid != null) {
            return parsedUuid;
        }

        File baseDir = new File(DATA_DIR);
        File[] eventFolders = baseDir.listFiles(File::isDirectory);

        if (eventFolders == null) {
            throw new IllegalStateException("No events found");
        }

        return Arrays.stream(eventFolders)
                .filter(folder -> {
                    File eventFile = new File(folder, "event.json");
                    if (!eventFile.exists()) return false;

                    try (FileReader reader = new FileReader(eventFile)) {
                        Event e = gson.fromJson(reader, Event.class);
                        return input.equalsIgnoreCase(e.getName());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(folder -> tryParseUuid(folder.getName()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Event not found: " + input));
    }

    private UUID tryParseUuid(String value) {
        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private UUID getLatestEventUuid() {
        File baseDir = new File(DATA_DIR);

        File[] eventFolders = baseDir.listFiles(File::isDirectory);

        if (eventFolders == null || eventFolders.length == 0) {
            throw new IllegalStateException("No events found");
        }

        File latest = Arrays.stream(eventFolders)
                .filter(f -> new File(f, "event.json").exists())
                .max(Comparator.comparingLong(this::getEventCreationTimeSafe))
                .orElseThrow(() -> new IllegalStateException("No valid events found"));

        return UUID.fromString(latest.getName());
    }

    private long getEventCreationTimeSafe(File eventFolder) {
        try (FileReader reader = new FileReader(new File(eventFolder, "event.json"))) {
            Event event = gson.fromJson(reader, Event.class);
            return event.getCreationTime();
        } catch (Exception e) {
            return 0L;
        }
    }

    public void parseRawData(String fileName, int id, String eventName) {
        // 1. Event-UUID auflösen (berücksichtigt null -> neuestes Event)
        UUID eventUuid = resolveEventUuid(eventName);
        File eventFolder = new File(DATA_DIR + eventUuid);

        // 2. Eingabedatei aus dem Event-Ordner referenzieren
        File inputFile = new File(eventFolder, fileName);

        if (!inputFile.exists()) {
            throw new IllegalStateException("Raw file not found: " + fileName + " in event folder: " + eventUuid);
        }

        JsonObject root;

        try (FileReader reader = new FileReader(inputFile)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read raw file", e);
        }

        JsonObject data = root.getAsJsonObject("data");
        // --- parseRawData ---

        JsonObject seedJson = data.has("seed") && !data.get("seed").isJsonNull()
                ? data.getAsJsonObject("seed")
                : new JsonObject();

        String seedType = seedJson.has("overworld") && !seedJson.get("overworld").isJsonNull()
                ? seedJson.get("overworld").getAsString()
                : null;

        String bastionType = seedJson.has("nether") && !seedJson.get("nether").isJsonNull()
                ? seedJson.get("nether").getAsString()
                : null;

        int[] endTowers = seedJson.has("endTowers") && !seedJson.get("endTowers").isJsonNull()
                ? gson.fromJson(seedJson.get("endTowers"), int[].class)
                : new int[0];

        String[] variations = seedJson.has("variations") && !seedJson.get("variations").isJsonNull()
                ? gson.fromJson(seedJson.get("variations"), String[].class)
                : new String[0];

// completions-Block (bereits has()-Check vorhanden, UUID-Parsing absichern)
        Map<UUID, Long> completions = new HashMap<>();
        if (data.has("completions") && !data.get("completions").isJsonNull()) {
            data.getAsJsonArray("completions").forEach(el -> {
                JsonObject obj = el.getAsJsonObject();

                if (!obj.has("uuid") || obj.get("uuid").isJsonNull()
                        || !obj.has("time") || obj.get("time").isJsonNull()) {
                    return; // unvollständigen Eintrag überspringen
                }

                String uuidStr = obj.get("uuid").getAsString();

                if (uuidStr.length() == 32 && !uuidStr.contains("-")) {
                    uuidStr = uuidStr.substring(0, 8) + "-" +
                            uuidStr.substring(8, 12) + "-" +
                            uuidStr.substring(12, 16) + "-" +
                            uuidStr.substring(16, 20) + "-" +
                            uuidStr.substring(20);
                }

                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    long time = obj.get("time").getAsLong();
                    completions.put(uuid, time);
                } catch (IllegalArgumentException e) {
                    System.err.println("Ungültige UUID in completions übersprungen: " + uuidStr);
                }
            });
        }

        long startedAt = data.has("date")
                ? data.get("date").getAsLong()
                : System.currentTimeMillis();

        UUID uuid = UUID.randomUUID();

        Seed seed = new Seed(
                uuid,
                id,
                startedAt,
                completions,
                seedType,
                bastionType,
                endTowers,
                variations
        );

        if (!eventFolder.exists()) {
            eventFolder.mkdirs();
        }

        File outFile = new File(eventFolder, "s" + id + ".json");

        try (FileWriter writer = new FileWriter(outFile)) {
            gson.toJson(seed, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not write seed file", e);
        }
    }

    public void updatePlayer(Player player, String eventName) {
        UUID eventUuid = resolveEventUuid(eventName);

        File playerFolder = new File(DATA_DIR + eventUuid + "/players");

        if (!playerFolder.exists() || !playerFolder.isDirectory()) {
            throw new IllegalStateException("Player folder does not exist for event: " + eventUuid);
        }

        File[] playerFiles = playerFolder.listFiles((dir, name) ->
                name.endsWith(".json") && name.contains(player.getUuid().toString())
        );

        if (playerFiles == null || playerFiles.length == 0) {
            throw new IllegalStateException("No player file found for UUID: " + player.getUuid());
        }
        File targetFile = playerFiles[0];

        try (FileWriter writer = new FileWriter(targetFile, false)) {
            gson.toJson(player, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not update player file for UUID: " + player.getUuid(), e);
        }
    }

    public List<Seed> loadSeeds(String eventName) {
        UUID eventUuid = resolveEventUuid(eventName);
        File eventFolder = new File(DATA_DIR + eventUuid);

        List<Seed> seeds = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            File seedFile = new File(eventFolder, "s" + i + ".json");

            if (!seedFile.exists()) {
                continue;
            }

            try (FileReader reader = new FileReader(seedFile)) {
                Seed seed = gson.fromJson(reader, Seed.class);
                seeds.add(seed);
            } catch (IOException e) {
                System.err.println("Could not read seed file: " + seedFile.getName());
            }
        }

        return seeds;
    }


}