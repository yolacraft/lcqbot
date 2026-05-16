package de.yolacraft.lcqbot.storage.v2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.yolacraft.lcqbot.model.v2.Event;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

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
}