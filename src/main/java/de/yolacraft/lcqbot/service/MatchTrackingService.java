package de.yolacraft.lcqbot.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class MatchTrackingService {

    private final FileStorageService storage;
    private final JDA jda;
    private final HttpClient httpClient;
    private final Gson gson;
    private volatile boolean isDone;

    private volatile String latestLiveDataRaw = null;
    private volatile boolean isTracking = false;

    private final Set<Integer> knownMatchIds = new HashSet<>();

    public MatchTrackingService(FileStorageService storage, @Lazy JDA jda) {
        this.storage = storage;
        this.jda = jda;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public void startTracking(String eventName, String hostName, MessageChannelUnion channel) {
        isTracking = true;
        isDone = false;
        latestLiveDataRaw = null;
        knownMatchIds.clear();

        CompletableFuture.runAsync(() -> {
            boolean initialFetchDone = false;

            try {
                while (!isDone) {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.mcsrranked.com/users/" + hostName + "/matches"))
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    System.out.println("Fetching matches for " + hostName);

                    if (response.statusCode() == 200) {
                        String body = response.body();
                        latestLiveDataRaw = body;

                        System.out.println("Successfully fetched matches for " + hostName);

                        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
                        JsonArray matches = root.getAsJsonArray("data");

                        if (!initialFetchDone) {
                            for (int i = 0; i < matches.size(); i++) {
                                int id = matches.get(i).getAsJsonObject().get("id").getAsInt();
                                knownMatchIds.add(id);
                            }
                            initialFetchDone = true;
                        } else {
                            for (int i = 0; i < matches.size(); i++) {
                                JsonObject match = matches.get(i).getAsJsonObject();
                                int id = match.get("id").getAsInt();

                                if (!knownMatchIds.contains(id)) {
                                    knownMatchIds.add(id);
                                    System.out.println("Found match for " + hostName + ":" + id);
                                    channel.sendMessage("Match Found: `" + id + "`").queue();

                                }
                            }
                        }
                    }
                    if (!isDone) {
                        Thread.sleep(10000);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isTracking = false;
            }
        });
    }

    public String getLatestLiveDataRaw() {
        return latestLiveDataRaw;
    }

    public boolean isTracking() {
        return isTracking;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}