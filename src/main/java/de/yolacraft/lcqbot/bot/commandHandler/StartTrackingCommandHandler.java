package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.EventResolver;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.EventStatus;
import de.yolacraft.lcqbot.service.MatchTrackingService;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class StartTrackingCommandHandler {

    private final EventResolver resolver;
    private final RoleGuard roleGuard;
    private final MatchTrackingService trackingService;
    private final FileStorageService storage;

    public StartTrackingCommandHandler(EventResolver resolver, RoleGuard roleGuard, MatchTrackingService trackingService, FileStorageService storage) {
        this.resolver = resolver;
        this.roleGuard = roleGuard;
        this.trackingService = trackingService;
        this.storage = storage;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        Optional<Event> opt = resolver.resolveAndReply(interaction);
        if (opt.isEmpty()) return;
        Event event = opt.get();

        if (!roleGuard.checkAndReply(interaction, event.getId())) return;

        if (event.getStatus() == EventStatus.FINISHED || event.getCurrentSeed() >= 7) {
            interaction.reply("Das Event ist nach 7 Seeds beendet. Es kann kein weiteres Tracking gestartet werden.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String host = interaction.getOption("host").getAsString();

        // Erhöhe die Seed-Nummer
        int currentSeed = event.getCurrentSeed() + 1;
        event.setCurrentSeed(currentSeed);
        storage.saveEvent(event);

        trackingService.startTracking(event, host, currentSeed);

        interaction.reply(MessageTemplates.formatTrackingStarted(currentSeed, host))
                .queue();
    }
}