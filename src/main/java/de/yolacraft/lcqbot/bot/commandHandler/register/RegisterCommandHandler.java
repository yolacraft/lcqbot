package de.yolacraft.lcqbot.bot.commandHandler.register;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.bot.utils.MessageEncoder;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Component
public class RegisterCommandHandler {

    private final FileStorageService storage;
    private final RoleGuard roleGuard;
    private final Gson gson = new Gson();

    public RegisterCommandHandler(FileStorageService storage, RoleGuard roleGuard) {
        this.storage = storage;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) throws Exception {

        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) {
            return;
        }

        interaction.deferReply().queue();

        CompletableFuture.runAsync(() -> {
            try {
                String ign = interaction.getOption("ign").getAsString();
                String twitch = interaction.getOption("twitch").getAsString();
                
                // Verwende den optionalen User Parameter, ansonsten den Befehlssender
                net.dv8tion.jda.api.entities.User targetUser = interaction.getOption("user") != null 
                        ? interaction.getOption("user").getAsUser()
                        : interaction.getUser();

                URL url = new URL("https://api.mcsrranked.com/users/" + ign);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                if (connection.getResponseCode() != 200) {
                    interaction.getHook().sendMessage(MessageEncoder.encode("register.player_not_found"))
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                JsonObject json = gson.fromJson(
                        new InputStreamReader(connection.getInputStream()),
                        JsonObject.class
                );

                JsonObject data = json.getAsJsonObject("data");

                if (data == null || data.get("uuid") == null) {
                    interaction.getHook().sendMessage(MessageEncoder.encode("register.uuid_not_found"))
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                String mcuuid = data.get("uuid").getAsString();

                Player p = new Player(
                        targetUser.getId(),
                        mcuuid,
                        twitch,
                        ign
                );

                storage.addPlayer(p, null, targetUser.getName());

                BufferedImage img = ImageGenerator.createImage(ign, twitch);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);

                interaction.getHook()
                    .sendMessage(MessageEncoder.encode("register.success"))
                    .addFiles(FileUpload.fromData(baos.toByteArray(), "register.png"))
                    .queue();

            } catch (Exception e) {
                interaction.getHook()
                        .sendMessage(MessageEncoder.encode("register.error"))
                        .queue();
                e.printStackTrace();
            }
        });
    }
}