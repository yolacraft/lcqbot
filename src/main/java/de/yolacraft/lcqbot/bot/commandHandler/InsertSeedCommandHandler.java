package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.utils.MessageEncoder;
import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.service.LeaderboardService;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class InsertSeedCommandHandler {

    private final RoleGuard roleGuard;
    private final FileStorageService storage;
    private final LeaderboardService leaderboardService;

    public InsertSeedCommandHandler(RoleGuard roleGuard, FileStorageService storage, LeaderboardService leaderboardService) {
        this.roleGuard = roleGuard;
        this.storage = storage;
        this.leaderboardService = leaderboardService;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        OptionMapping opt = interaction.getOption("event");
        String eventName = null;
        if (opt != null) eventName = opt.getAsString();
        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) return;

        Event event = storage.getEvent(eventName);

        Long id          = interaction.getOption("id").getAsLong();
        int seedNumber   = interaction.getOption("seednumber").getAsInt();
        UUID eventId     = event.getUuid();

        String filename = UUID.randomUUID() + ".json";
        String url = "https://api.mcsrranked.com/matches/" + id;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                interaction.reply(MessageEncoder.format("fetch_failed")).queue();
                return;
            }

            Path targetDir  = Path.of("data", "v2", eventId.toString());
            Path targetFile = targetDir.resolve(filename);
            Files.createDirectories(targetDir);
            Files.writeString(targetFile, response.body());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            interaction.reply(MessageEncoder.format("fetch_failed")).queue();
            return;
        }

        storage.parseRawData(filename, seedNumber, eventName);
        
        // Update leaderboard after seed insertion
        leaderboardService.updateLeaderboardAndEliminate(event, seedNumber);
        
        interaction.reply(MessageEncoder.format("inserted_successfully")).queue();
    }
}