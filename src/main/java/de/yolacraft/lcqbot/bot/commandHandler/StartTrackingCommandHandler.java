package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.utils.MessageEncoder;
import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.service.MatchTrackingService;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

@Component
public class StartTrackingCommandHandler {

    private final RoleGuard roleGuard;
    private final MatchTrackingService trackingService;
    private final FileStorageService storage;

    public StartTrackingCommandHandler(RoleGuard roleGuard, MatchTrackingService trackingService, FileStorageService storage) {
        this.roleGuard = roleGuard;
        this.trackingService = trackingService;
        this.storage = storage;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        OptionMapping opt = interaction.getOption("event");
        String eventName = null;
        if(opt != null) eventName = opt.getAsString();

        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) return;

        String host = interaction.getOption("host").getAsString();

        trackingService.startTracking(eventName, host, interaction.getChannel());

        interaction.reply(MessageEncoder.format("tracking_started", host))
                .queue();
    }
}