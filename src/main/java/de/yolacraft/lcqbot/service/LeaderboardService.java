package de.yolacraft.lcqbot.service;

import de.yolacraft.lcqbot.bot.DiscordMessageSender;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.model.*;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.EventStatus;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<Player> allPlayers = storage.loadPlayers(event.getId());
        Map<String, Integer> playerPoints = calculatePoints(event.getId(), allPlayers);

        List<Player> activePlayers = getSortedActivePlayers(allPlayers, playerPoints);
        List<Player> playersToEliminate = new ArrayList<>();

        if (completedSeedNumber == 7) {
            activePlayers = adjustForTies(getSortedAllPlayers(allPlayers, playerPoints), 12, playerPoints);
        } else {
            int toKeep = calculateToKeep(activePlayers, completedSeedNumber, playerPoints);
            if (toKeep < activePlayers.size()) {
                toKeep = applyTieRule(activePlayers, toKeep, playerPoints);
                playersToEliminate = new ArrayList<>(activePlayers.subList(toKeep, activePlayers.size()));
                activePlayers = new ArrayList<>(activePlayers.subList(0, toKeep));
            }
        }

        processEliminations(event, playersToEliminate);

        String title = (completedSeedNumber == 7)
                ? MessageTemplates.formatLiveLeaderboardFinishedTitle(completedSeedNumber)
                : MessageTemplates.formatLiveLeaderboardTitle(completedSeedNumber);

        sendLeaderboard(event, activePlayers, playersToEliminate, playerPoints, title);
    }

    public void updateLeaderboardOnly(Event event) {
        List<Player> allPlayers = storage.loadPlayers(event.getId());
        Map<String, Integer> playerPoints = calculatePoints(event.getId(), allPlayers);

        List<Player> displayPlayers;
        String title;

        if (event.getStatus() == EventStatus.FINISHED || event.getCurrentSeed() >= 7) {
            displayPlayers = adjustForTies(getSortedAllPlayers(allPlayers, playerPoints), 12, playerPoints);
            title = MessageTemplates.formatLiveLeaderboardFinishedTitle(event.getCurrentSeed());
        } else {
            displayPlayers = getSortedActivePlayers(allPlayers, playerPoints);
            title = "Live Leaderboard";
        }

        sendLeaderboard(event, displayPlayers, List.of(), playerPoints, title);
    }

    private Map<String, Integer> calculatePoints(String eventId, List<Player> players) {
        Map<String, Integer> points = players.stream().collect(Collectors.toMap(Player::getId, p -> 0));
        storage.loadSeeds(eventId).stream()
                .filter(s -> s.getStatus() == SeedStatus.CONFIRMED)
                .flatMap(s -> s.getPlacements().stream())
                .forEach(pl -> points.merge(pl.getPlayerId(), pl.getPoints(), Integer::sum));
        return points;
    }

    private List<Player> getSortedActivePlayers(List<Player> players, Map<String, Integer> points) {
        return players.stream()
                .filter(p -> !p.isEliminated())
                .sorted((p1, p2) -> points.get(p2.getId()).compareTo(points.get(p1.getId())))
                .collect(Collectors.toList());
    }

    private List<Player> getSortedAllPlayers(List<Player> players, Map<String, Integer> points) {
        return players.stream()
                .sorted((p1, p2) -> points.get(p2.getId()).compareTo(points.get(p1.getId())))
                .collect(Collectors.toList());
    }

    private int calculateToKeep(List<Player> active, int seed, Map<String, Integer> points) {
        return switch (seed) {
            case 2 -> (int) active.stream().filter(p -> points.get(p.getId()) > 0).count();
            case 3 -> (int) Math.ceil(active.size() / 2.0);
            case 5 -> Math.min(8, active.size());
            case 6 -> Math.min(4, active.size());
            default -> active.size();
        };
    }

    private int applyTieRule(List<Player> players, int toKeep, Map<String, Integer> points) {
        if (toKeep <= 0 || toKeep >= players.size()) return toKeep;
        int lastScore = points.get(players.get(toKeep - 1).getId());
        while (toKeep < players.size() && points.get(players.get(toKeep).getId()) == lastScore) {
            toKeep++;
        }
        return toKeep;
    }

    private void processEliminations(Event event, List<Player> toEliminate) {
        if (toEliminate.isEmpty()) return;

        Guild guild = jdaProvider.getObject().getTextChannelById(event.getResultsChannelId()).getGuild();
        Role playerRole = guild.getRoleById(event.getActivePlayerRoleId());

        for (Player p : toEliminate) {
            p.setEliminated(true);
            storage.savePlayer(p);
            if (playerRole != null) {
                guild.retrieveMemberById(p.getDiscordUserId()).queue(
                        m -> guild.removeRoleFromMember(m, playerRole).queue(), e -> {}
                );
            }
        }
    }

    private void sendLeaderboard(Event event, List<Player> active, List<Player> eliminated, Map<String, Integer> points, String title) {
        StringBuilder sb = new StringBuilder("**").append(title).append("**\n\n");

        for (int i = 0; i < active.size(); i++) {
            Player p = active.get(i);
            sb.append("**").append(i + 1).append(".** ").append(p.getIngameName())
                    .append(" - ").append(points.get(p.getId())).append(" Punkte\n");
        }

        if (!eliminated.isEmpty()) {
            sb.append("\n💀 **Eliminiert:**\n");
            eliminated.forEach(p -> sb.append("~~").append(p.getIngameName()).append("~~\n"));
        }

        String channelId = event.getLiveboardChannelId() != null ? event.getLiveboardChannelId() : event.getResultsChannelId();
        TextChannel channel = jdaProvider.getObject().getTextChannelById(channelId);

        if (channel != null) {
            DiscordMessageSender.deleteMessages(channel, event.getLeaderboardMessageIds());
            DiscordMessageSender.sendSplitTextMessages(channel, sb.toString(), ids -> {
                event.setLeaderboardMessageIds(ids);
                storage.saveEvent(event);
            });
        }
    }

    private List<Player> adjustForTies(List<Player> sorted, int target, Map<String, Integer> points) {
        return new ArrayList<>(sorted.subList(0, applyTieRule(sorted, target, points)));
    }
}