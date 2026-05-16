package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.EventStatus;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InitCommandHandler {

    private final FileStorageService storage;
    private final RoleGuard roleGuard;

    public InitCommandHandler(FileStorageService storage, RoleGuard roleGuard) {
        this.storage = storage;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) return;

        String name   = interaction.getOption("name").getAsString();
        String apiKey = interaction.getOption("apikey").getAsString();

        Event event = new Event();
        event.setId(UUID.randomUUID().toString());
        event.setName(name);
        event.setApiKey(apiKey);
        event.setStatus(EventStatus.CREATED);
        event.setCreatedAt(System.currentTimeMillis());
        event.setInitiatedChannelId(interaction.getChannelId()); // für später

        storage.saveEvent(event);

        interaction.reply(MessageTemplates.formatEventCreated(name, event.getId()))
                .queue();
    }
}