package de.yolacraft.lcqbot.service;

import com.google.gson.Gson;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.model.*;
import de.yolacraft.lcqbot.model.api.McsrResponse;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class MatchTrackingService {

    private final FileStorageService storage;
    private final JDA jda;
    private final HttpClient httpClient;
    private final Gson gson;

    private volatile String latestLiveDataRaw = null;
    private volatile boolean isTracking = false;

    public MatchTrackingService(FileStorageService storage, @Lazy JDA jda) {
        this.storage = storage;
        this.jda = jda;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public void startTracking(Event event, String hostName, int seedNumber) {
        isTracking = true;
        latestLiveDataRaw = null;

        CompletableFuture.runAsync(() -> {
            try {
                boolean isDone = false;
                while (!isDone) {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.mcsrranked.com/users/" + hostName + "/live"))
                            .header("Private-Key", event.getApiKey())
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        latestLiveDataRaw = response.body();
                        McsrResponse mcsrResponse = gson.fromJson(latestLiveDataRaw, McsrResponse.class);


                        if (mcsrResponse.data != null && "done".equalsIgnoreCase(mcsrResponse.data.status)) {
                            isDone = true;
                            processFinishedMatch(event, mcsrResponse, seedNumber);
                        }
                    }

                    if (!isDone) {
                        Thread.sleep(15000); // Alle 10 Sekunden checken
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void processFinishedMatch(Event event, McsrResponse response, int seedNumber) {
        List<Player> allPlayers = storage.loadPlayers(event.getId());
        List<Placement> placements = new ArrayList<>();

        List<McsrResponse.McsrCompletion> completions = response.data.completions != null ? response.data.completions : new ArrayList<>();
        completions.sort(Comparator.comparingLong(c -> c.time));

        int currentPlace = 1;
        Set<String> finishedUuids = new HashSet<>();

        for (McsrResponse.McsrCompletion comp : completions) {
            finishedUuids.add(comp.uuid);

            // Finde den Ingame-Namen anhand der UUID aus dem API response.data.players array
            String inGameName = response.data.players.stream()
                    .filter(p -> p.uuid.equals(comp.uuid))
                    .map(p -> p.nickname)
                    .findFirst()
                    .orElse(null);

            if (inGameName == null) continue;

            // Finde unseren Bot-Player anhand des Ingame Namens (case insensitive)
            Optional<Player> botPlayerOpt = allPlayers.stream()
                    .filter(p -> p.getIngameName().equalsIgnoreCase(inGameName) && !p.isEliminated())
                    .findFirst();

            if (botPlayerOpt.isPresent()) {
                Player p = botPlayerOpt.get();
                Placement placement = new Placement(p.getId(), true, currentPlace, comp.time);

                // Punkteberechnung: 1. Platz 12 Punkte, 2. 11 ... ab 13. Platz 1 Punkt
                int points = currentPlace <= 12 ? (13 - currentPlace) : 1;
                placement.setPoints(points);

                placements.add(placement);
                currentPlace++;
            }
        }

        // Spieler, die es nicht geschafft haben (0 Punkte)
        for (Player p : allPlayers) {
            if (p.isEliminated()) continue;

            boolean finished = placements.stream().anyMatch(pl -> pl.getPlayerId().equals(p.getId()));
            if (!finished) {
                Placement noFinish = new Placement(p.getId(), false, 999, 0);
                noFinish.setPoints(0);
                placements.add(noFinish);
            }
        }

        // Seed speichern oder aktualisieren
        List<Seed> existingSeeds = storage.loadSeeds(event.getId());
        Seed seed = existingSeeds.stream()
                .filter(s -> s.getSeedNumber() == seedNumber)
                .findFirst()
                .orElseGet(() -> {
                    Seed newSeed = new Seed();
                    newSeed.setId(UUID.randomUUID().toString());
                    newSeed.setEventId(event.getId());
                    newSeed.setSeedNumber(seedNumber);
                    newSeed.setStartedAt(System.currentTimeMillis());
                    return newSeed;
                });

        seed.setStatus(SeedStatus.PENDING);
        seed.setPlacements(placements);

        storage.saveSeed(seed);

        // Nachricht im Kanal, in dem /init ausgeführt wurde
        TextChannel initiatedChannel = jda.getTextChannelById(event.getInitiatedChannelId());
        if (initiatedChannel != null) {
            initiatedChannel.sendMessage(MessageTemplates.formatSeedFinished(seedNumber)).queue();
        }
    }


    public String getLatestLiveDataRaw() {
        return latestLiveDataRaw;
    }

    public boolean isTracking() {
        return isTracking;
    }
}