package de.yolacraft.lcqbot.seasonleaderboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeasonLeaderboardService {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path LATEST_GLOBAL_FILE = DATA_DIR.resolve("latest_global_leaderboard.json");

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SeasonLeaderboardService() {
        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            throw new SeasonLeaderboardException("Unable to initialize leaderboard storage", e);
        }
    }

    public synchronized SeasonLeaderboard createEmptyLeaderboard(int season, int week, String channelId) {
        validateSeason(season);
        validateWeek(week);
        String id = UUID.randomUUID().toString();
        SeasonLeaderboard leaderboard = new SeasonLeaderboard(id, season, week, channelId, null, System.currentTimeMillis());
        leaderboard.setPlayers(new LinkedHashMap<>());
        writeLeaderboardFile(leaderboard);
        return leaderboard;
    }

    public synchronized SeasonLeaderboard loadLeaderboard(String leaderboardId) {
        String normalizedId = normalizeUuid(leaderboardId);
        Path file = getLeaderboardFile(normalizedId);
        if (!Files.exists(file)) {
            throw new SeasonLeaderboardException("Leaderboard file not found for id: " + normalizedId);
        }
        try {
            String raw = Files.readString(file);
            SeasonLeaderboard leaderboard = gson.fromJson(raw, SeasonLeaderboard.class);
            if (leaderboard == null) {
                throw new SeasonLeaderboardException("Leaderboard JSON is empty or invalid for id: " + normalizedId);
            }
            if (!normalizedId.equals(leaderboard.getId())) {
                leaderboard.setId(normalizedId);
            }
            validateSeason(leaderboard.getSeason());
            validateWeek(leaderboard.getWeek());
            normalizeLeaderboard(leaderboard);
            return leaderboard;
        } catch (JsonSyntaxException e) {
            throw new SeasonLeaderboardException("Corrupted leaderboard JSON for id: " + normalizedId, e);
        } catch (IOException e) {
            throw new SeasonLeaderboardException("Unable to read leaderboard file for id: " + normalizedId, e);
        }
    }

    public synchronized void saveLeaderboard(SeasonLeaderboard leaderboard) {
        if (leaderboard == null) {
            throw new SeasonLeaderboardException("Leaderboard cannot be null");
        }
        normalizeLeaderboard(leaderboard);
        writeLeaderboardFile(leaderboard);
    }

    public synchronized String loadLatestLeaderboardId() {
        if (!Files.exists(LATEST_GLOBAL_FILE)) {
            throw new SeasonLeaderboardException("No global leaderboard has been created yet");
        }
        try {
            String raw = Files.readString(LATEST_GLOBAL_FILE);
            LatestGlobalLeaderboardMetadata metadata = gson.fromJson(raw, LatestGlobalLeaderboardMetadata.class);
            if (metadata == null || metadata.getLatestLeaderboardId() == null || metadata.getLatestLeaderboardId().isBlank()) {
                throw new SeasonLeaderboardException("Latest global leaderboard metadata is missing or invalid");
            }
            return normalizeUuid(metadata.getLatestLeaderboardId());
        } catch (JsonSyntaxException e) {
            throw new SeasonLeaderboardException("Corrupted latest global leaderboard metadata", e);
        } catch (IOException e) {
            throw new SeasonLeaderboardException("Unable to read latest global leaderboard metadata", e);
        }
    }

    public synchronized void saveLatestLeaderboardId(String leaderboardId) {
        String normalizedId = normalizeUuid(leaderboardId);
        writeJsonAtomically(LATEST_GLOBAL_FILE, new LatestGlobalLeaderboardMetadata(normalizedId));
    }

    public synchronized SeasonLeaderboard loadLeaderboardOrLatest(String leaderboardId) {
        if (leaderboardId == null || leaderboardId.isBlank()) {
            leaderboardId = loadLatestLeaderboardId();
        }
        return loadLeaderboard(leaderboardId);
    }

    public synchronized void addOrUpdatePlayerPoints(SeasonLeaderboard leaderboard, String rawPlayerName, int week, int points) {
        if (leaderboard == null) {
            throw new SeasonLeaderboardException("Leaderboard not found");
        }
        String normalizedName = normalizePlayerName(rawPlayerName);
        validateWeek(week);
        if (points < 0) {
            throw new SeasonLeaderboardException("Points must not be negative");
        }
        SeasonLeaderboardEntry entry = leaderboard.getPlayers().get(normalizedName);
        if (entry == null) {
            entry = new SeasonLeaderboardEntry();
        }
        entry.setWeek(week - 1, points);
        leaderboard.getPlayers().put(normalizedName, entry);
        normalizeLeaderboard(leaderboard);
    }

    public MessageEmbed buildEmbed(SeasonLeaderboard leaderboard) {
        String title = String.format("Globales Leaderboard S%dW%d", leaderboard.getSeason(), leaderboard.getWeek());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);

        List<Map.Entry<String, SeasonLeaderboardEntry>> sortedPlayers = sortPlayers(leaderboard);
        if (sortedPlayers.isEmpty()) {
            embedBuilder.setDescription("No entries yet.");
            return embedBuilder.build();
        }

        int displayCount = Math.min(sortedPlayers.size(), 20);
        int linesToShow = sortedPlayers.size() > 20 ? 19 : displayCount;
        StringBuilder description = new StringBuilder();

        for (int i = 0; i < linesToShow; i++) {
            Map.Entry<String, SeasonLeaderboardEntry> entry = sortedPlayers.get(i);
            description.append(formatPlayerRow(i + 1, entry.getKey(), entry.getValue()));
            if (i < linesToShow - 1) {
                description.append("\n");
            }
        }

        embedBuilder.setDescription(description.toString());

        if (sortedPlayers.size() > 20) {
            int remaining = sortedPlayers.size() - 19;
            embedBuilder.addField("and " + remaining + " more...", "\u200b", false);
        }

        return embedBuilder.build();
    }

    public String buildText(SeasonLeaderboard leaderboard) {
        String title = String.format("Globales Leaderboard S%dW%d", leaderboard.getSeason(), leaderboard.getWeek());
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("**").append(title).append("**\n\n");

        List<Map.Entry<String, SeasonLeaderboardEntry>> sortedPlayers = sortPlayers(leaderboard);
        if (sortedPlayers.isEmpty()) {
            textBuilder.append("No entries yet.");
            return textBuilder.toString();
        }

        int displayCount = Math.min(sortedPlayers.size(), 20);
        int linesToShow = sortedPlayers.size() > 20 ? 19 : displayCount;

        for (int i = 0; i < linesToShow; i++) {
            Map.Entry<String, SeasonLeaderboardEntry> entry = sortedPlayers.get(i);
            textBuilder.append(formatPlayerRow(i + 1, entry.getKey(), entry.getValue()));
            if (i < linesToShow - 1) {
                textBuilder.append("\n");
            }
        }

        if (sortedPlayers.size() > 20) {
            int remaining = sortedPlayers.size() - 19;
            textBuilder.append("\n\nand ").append(remaining).append(" more...");
        }

        return textBuilder.toString();
    }

    private void validateSeason(int season) {
        if (season < 1) {
            throw new SeasonLeaderboardException("Season must be a positive integer");
        }
    }

    private void validateWeek(int week) {
        if (week < 1 || week > 4) {
            throw new SeasonLeaderboardException("Week must be between 1 and 4");
        }
    }

    private String normalizeUuid(String value) {
        if (value == null || value.isBlank()) {
            throw new SeasonLeaderboardException("Leaderboard id is required");
        }
        try {
            return UUID.fromString(value).toString();
        } catch (IllegalArgumentException e) {
            throw new SeasonLeaderboardException("Invalid leaderboard UUID: " + value, e);
        }
    }

    private String normalizePlayerName(String value) {
        if (value == null) {
            throw new SeasonLeaderboardException("Player name is required");
        }
        String normalized = value.trim().toLowerCase();
        if (normalized.isBlank()) {
            throw new SeasonLeaderboardException("Player name must not be empty");
        }
        return normalized;
    }

    private void normalizeLeaderboard(SeasonLeaderboard leaderboard) {
        leaderboard.normalizePlayers();
        if (leaderboard.getPlayers() == null) {
            leaderboard.setPlayers(new LinkedHashMap<>());
        }
    }

    private List<Map.Entry<String, SeasonLeaderboardEntry>> sortPlayers(SeasonLeaderboard leaderboard) {
        return leaderboard.getPlayers().entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, SeasonLeaderboardEntry> entry) -> entry.getValue().getTotal()).reversed()
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toList());
    }

    private String formatPlayerRow(int rank, String playerName, SeasonLeaderboardEntry entry) {
        int[] weekValues = entry.getWeeks();
        StringBuilder weekParts = new StringBuilder();
        int nonZeroCount = 0;

        for (int value : weekValues) {
            if (value != 0) {
                if (weekParts.length() > 0) {
                    weekParts.append("+");
                }
                weekParts.append(value);
                nonZeroCount++;
            }
        }

        String result = String.format("**%d.** %s - **%d** Punkte", rank, playerName, entry.getTotal());
        if (nonZeroCount > 1) {
            result += " (" + weekParts + ")";
        }
        return result;
    }

    private Path getLeaderboardFile(String leaderboardId) {
        return DATA_DIR.resolve("leaderboard_" + leaderboardId + ".json");
    }

    private void writeLeaderboardFile(SeasonLeaderboard leaderboard) {
        Path file = getLeaderboardFile(leaderboard.getId());
        writeJsonAtomically(file, leaderboard);
    }

    private void writeJsonAtomically(Path target, Object data) {
        try {
            Path temp = Files.createTempFile(DATA_DIR, "tmp_leaderboard_", ".json");
            Files.writeString(temp, gson.toJson(data));
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new SeasonLeaderboardException("Unable to write leaderboard storage", e);
        }
    }
}
