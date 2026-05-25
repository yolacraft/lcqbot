package de.yolacraft.lcqbot.service;

import de.yolacraft.lcqbot.bot.utils.DiscordMessageSender;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.model.Seed;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final FileStorageService storage;
    private final ObjectProvider<JDA> jdaProvider;

    public LeaderboardService(FileStorageService storage, ObjectProvider<JDA> jdaProvider) {
        this.storage = storage;
        this.jdaProvider = jdaProvider;
    }

    public void updateLeaderboardAndEliminate(Event event, int completedSeedNumber) {
        String eventId = event.getUuid().toString();
        List<Player> allPlayers = storage.getPlayers(eventId);
        Map<UUID, Integer> playerPoints = calculatePoints(eventId, allPlayers);

        // Berechne voll-dynamisch die Historie bis zum vorherigen und zum aktuellen Seed
        Set<UUID> eliminatedBefore = calculateEliminatedPlayerIds(eventId, allPlayers, completedSeedNumber - 1);
        Set<UUID> eliminatedNow = calculateEliminatedPlayerIds(eventId, allPlayers, completedSeedNumber);

        // Nur Spieler, die JETZT in diesem Seed neu fliegen, bekommen die Rolle entzogen
        List<Player> playersToEliminate = allPlayers.stream()
                .filter(p -> eliminatedNow.contains(p.getUuid()) && !eliminatedBefore.contains(p.getUuid()))
                .collect(Collectors.toList());

        processEliminations(event, playersToEliminate);

        // Daten für die Anzeige frisch aus dem Speicher holen & sortieren
        List<Player> updatedPlayers = storage.getPlayers(eventId);
        List<Player> sortedAll = getSortedAllPlayers(updatedPlayers, playerPoints);

        String title;
        boolean showEliminated;

        if (completedSeedNumber >= 7) {
            title = "Leaderboard 7/7 Seeds - Top 12";
            showEliminated = false; // Blendet nach Seed 7 alle aus, die laut Berechnung eliminiert sind
        } else {
            title = "Leaderboard " + completedSeedNumber + "/7 Seeds";
            showEliminated = true;
        }

        sendLeaderboard(event, sortedAll, eliminatedNow, playerPoints, title, showEliminated);
    }

    public void updateLeaderboardOnly(Event event) {
        String eventId = event.getUuid().toString();
        List<Player> allPlayers = storage.getPlayers(eventId);
        Map<UUID, Integer> playerPoints = calculatePoints(eventId, allPlayers);

        List<Seed> seeds = storage.loadSeeds(eventId);
        int currentSeedNumber = Math.max(1, seeds.size());

        // Berechne den aktuellen Stand der Eliminierungen live aus den Daten
        Set<UUID> eliminatedNow = calculateEliminatedPlayerIds(eventId, allPlayers, currentSeedNumber);
        List<Player> displayPlayers = getSortedAllPlayers(allPlayers, playerPoints);

        String title = "Live Leaderboard";
        boolean showEliminated = (currentSeedNumber < 7);

        sendLeaderboard(event, displayPlayers, eliminatedNow, playerPoints, title, showEliminated);
    }

    public List<de.yolacraft.lcqbot.api.LeaderboardEntryDto> getLeaderboard(String eventId) {
        List<Player> players = storage.getPlayers(eventId);
        Map<UUID, Integer> points = calculatePoints(eventId, players);

        List<Seed> seeds = storage.loadSeeds(eventId);
        int currentSeedNumber = Math.max(1, seeds.size());
        Set<UUID> eliminatedNow = calculateEliminatedPlayerIds(eventId, players, currentSeedNumber);

        return players.stream()
                .map(player -> new de.yolacraft.lcqbot.api.LeaderboardEntryDto(
                        player.getUuid().toString(),
                        player.getIgn(),
                        player.getTwitchUserName(),
                        points.getOrDefault(player.getUuid(), 0),
                        eliminatedNow.contains(player.getUuid()) // Liefert den live berechneten Zustand an die API
                ))
                .sorted((a, b) -> Integer.compare(b.points(), a.points()))
                .toList();
    }

    private Map<UUID, Integer> calculatePoints(String eventId, List<Player> players) {
        return calculatePointsUpTo(eventId, players, Integer.MAX_VALUE);
    }

    /**
     * Berechnet die Punktehistorie isoliert bis zu einem bestimmten Seed (1-basiert).
     */
    private Map<UUID, Integer> calculatePointsUpTo(String eventId, List<Player> players, int upToSeed) {
        Map<UUID, Integer> map = players.stream()
                .collect(Collectors.toMap(Player::getUuid, p -> 0));

        Map<UUID, UUID> minecraftToPlayer = players.stream()
                .map(player -> {
                    UUID mcUuid = tryParseUuid(player.getMinecraftUUID());
                    return mcUuid != null ? Map.entry(mcUuid, player.getUuid()) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

        List<Seed> seeds = storage.loadSeeds(eventId);
        int count = 0;
        for (Seed seed : seeds) {
            count++;
            if (count > upToSeed) {
                break;
            }
            Map<UUID, Long> cmpl = seed.getCompletions();
            if (cmpl == null) continue;

            List<Map.Entry<UUID, Long>> sorted = new ArrayList<>(cmpl.entrySet());
            sorted.sort(Map.Entry.comparingByValue());

            int pointsAwarded = 12;
            for (Map.Entry<UUID, Long> entry : sorted) {
                UUID playerId = minecraftToPlayer.get(entry.getKey());
                if (playerId != null) {
                    map.merge(playerId, pointsAwarded, Integer::sum);
                }

                if (pointsAwarded > 1) {
                    pointsAwarded--;
                }
            }
        }
        return map;
    }

    /**
     * Berechnet chronologisch Schritt für Schritt, wer zu welchem Zeitpunkt aus den Daten fliegt.
     * Nutzt strikte "< cutoffScore" Checks, wodurch Ties (Gleichstände) automatisch im Turnier verbleiben.
     */
    private Set<UUID> calculateEliminatedPlayerIds(String eventId, List<Player> allPlayers, int upToSeed) {
        Set<UUID> eliminated = new HashSet<>();

        for (int s = 1; s <= upToSeed; s++) {
            if (s == 1 || s == 4) {
                continue; // Seed 1 und Seed 4 haben keine Eliminierungen
            }

            // Punktestände exakt zum Zeitpunkt nach Abschluss dieses Seeds holen
            Map<UUID, Integer> pointsAtS = calculatePointsUpTo(eventId, allPlayers, s);

            // Filtert alle Spieler heraus, die VOR diesem Seed bereits flach lagen
            final Set<UUID> currentlyEliminated = new HashSet<>(eliminated);
            List<Player> active = allPlayers.stream()
                    .filter(p -> !currentlyEliminated.contains(p.getUuid()))
                    .sorted((p1, p2) -> Integer.compare(
                            pointsAtS.getOrDefault(p2.getUuid(), 0),
                            pointsAtS.getOrDefault(p1.getUuid(), 0)))
                    .collect(Collectors.toList());

            if (s == 2) {
                // Nach Seed 2: Alle mit exakt 0 Punkten fliegen raus
                for (Player p : active) {
                    if (pointsAtS.getOrDefault(p.getUuid(), 0) == 0) {
                        eliminated.add(p.getUuid());
                    }
                }
            }
            else if (s == 3) {
                // Nach Seed 3: Bottom 50% von denen, die MEHR als 0 Punkte haben
                List<Player> withPoints = active.stream()
                        .filter(p -> pointsAtS.getOrDefault(p.getUuid(), 0) > 0)
                        .collect(Collectors.toList());

                int totalWithPoints = withPoints.size();
                int keep = (int) Math.ceil(totalWithPoints / 2.0);

                if (keep > 0 && keep < withPoints.size()) {
                    int cutoffScore = pointsAtS.getOrDefault(withPoints.get(keep - 1).getUuid(), 0);
                    // Tie Rule: Nur wer ECHTE weniger Punkte hat als der Cutoff fliegt (Gleichstände bleiben drin)
                    for (Player p : active) {
                        if (pointsAtS.getOrDefault(p.getUuid(), 0) < cutoffScore) {
                            eliminated.add(p.getUuid());
                        }
                    }
                } else if (keep == 0) {
                    // Falls ausnahmsweise niemand Punkte hat, fliegt jeder mit 0 Punkten
                    for (Player p : active) {
                        if (pointsAtS.getOrDefault(p.getUuid(), 0) == 0) {
                            eliminated.add(p.getUuid());
                        }
                    }
                }
            }
            else if (s == 5) {
                // Nach Seed 5: Top 8 bleiben drin, Rest wird durchgestrichen / fliegt
                int keep = 8;
                if (keep < active.size()) {
                    int cutoffScore = pointsAtS.getOrDefault(active.get(keep - 1).getUuid(), 0);
                    for (Player p : active) {
                        if (pointsAtS.getOrDefault(p.getUuid(), 0) < cutoffScore) {
                            eliminated.add(p.getUuid());
                        }
                    }
                }
            }
            else if (s == 6) {
                // Nach Seed 6: Top 4 bleiben drin, Rest wird durchgestrichen / fliegt
                int keep = 4;
                if (keep < active.size()) {
                    int cutoffScore = pointsAtS.getOrDefault(active.get(keep - 1).getUuid(), 0);
                    for (Player p : active) {
                        if (pointsAtS.getOrDefault(p.getUuid(), 0) < cutoffScore) {
                            eliminated.add(p.getUuid());
                        }
                    }
                }
            }
            else if (s == 7) {
                // Nach Seed 7: Top 12 bleiben drin, Rest fliegt
                int keep = 12;
                if (keep < active.size()) {
                    int cutoffScore = pointsAtS.getOrDefault(active.get(keep - 1).getUuid(), 0);
                    for (Player p : active) {
                        if (pointsAtS.getOrDefault(p.getUuid(), 0) < cutoffScore) {
                            eliminated.add(p.getUuid());
                        }
                    }
                }
            }
        }
        return eliminated;
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
                    return UUID.fromString(
                            cleaned.substring(0, 8) + "-" +
                                    cleaned.substring(8, 12) + "-" +
                                    cleaned.substring(12, 16) + "-" +
                                    cleaned.substring(16, 20) + "-" +
                                    cleaned.substring(20)
                    );
                } catch (IllegalArgumentException ignored2) {
                    return null;
                }
            }
            return null;
        }
    }

    private List<Player> getSortedAllPlayers(List<Player> players, Map<UUID, Integer> points) {
        return players.stream()
                .sorted((p1, p2) -> Integer.compare(
                        points.getOrDefault(p2.getUuid(), 0),
                        points.getOrDefault(p1.getUuid(), 0)))
                .collect(Collectors.toList());
    }

    private void processEliminations(Event event, List<Player> toEliminate) {
        if (toEliminate.isEmpty()) return;

        Guild guild = jdaProvider.getObject().getTextChannelById(event.getResultChannelId()).getGuild();
        Role playerRole = guild.getRoleById(event.getPlayerRoleId());

        String eventId = event.getUuid().toString();
        for (Player p : toEliminate) {
            p.setEliminated(true);
            storage.updatePlayer(p, eventId);
            if (playerRole != null) {
                guild.retrieveMemberById(p.getDiscordUserId()).queue(
                        m -> guild.removeRoleFromMember(m, playerRole).queue(),
                        e -> {}
                );
            }
        }
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("~", "\\~")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("|", "\\|")
                .replace("`", "\\`");
    }

    private void sendLeaderboard(Event event, List<Player> players, Set<UUID> eliminatedIds,
                                 Map<UUID, Integer> points, String title, boolean showEliminated) {
        StringBuilder sb = new StringBuilder("**").append(title).append("**\n\n");

        int displayLimit = showEliminated ? 24 : players.size();

        // Wenn showEliminated == false (nach Seed 7): Werfen wir berechnete Leichen direkt raus
        List<Player> toDisplay = players.stream()
                .filter(p -> showEliminated || !eliminatedIds.contains(p.getUuid()))
                .limit(displayLimit)
                .collect(Collectors.toList());

        int rank = 1;
        for (Player p : toDisplay) {
            boolean eliminated = eliminatedIds.contains(p.getUuid());

            String escapedName = escapeMarkdown(p.getIgn());
            String name = eliminated ? "~~" + escapedName + "~~" : escapedName;

            int pts = points.getOrDefault(p.getUuid(), 0);

            sb.append("**").append(rank).append(".** ")
                    .append(name)
                    .append(" - ")
                    .append(pts)
                    .append(" Punkte\n");

            rank++;
        }

        String channelId = event.getLeaderboardChannelId() != null
                ? event.getLeaderboardChannelId()
                : event.getResultChannelId();
        TextChannel channel = jdaProvider.getObject().getTextChannelById(channelId);

        if (channel != null) {
            DiscordMessageSender.deleteMessages(channel, event.getLeaderboardMessageIds());
            DiscordMessageSender.sendSplitTextMessages(channel, sb.toString(), ids -> {
                event.setLeaderboardMessageIds(ids);
                storage.saveEvent(event);
            });
        }
    }
}