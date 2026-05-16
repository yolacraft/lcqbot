package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.EventResolver;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.EventStatus;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConfigCommandHandler {

    private final FileStorageService storage;
    private final EventResolver resolver;
    private final RoleGuard roleGuard;

    public ConfigCommandHandler(FileStorageService storage, EventResolver resolver, RoleGuard roleGuard) {
        this.storage = storage;
        this.resolver = resolver;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        Optional<Event> opt = resolver.resolveAndReply(interaction);
        if (opt.isEmpty()) return;

        Event event = opt.get();

        if (!roleGuard.checkAndReply(interaction, event.getId())) return;

        Role adminRole   = interaction.getOption("adminrole").getAsRole();
        Role playerRole  = interaction.getOption("playerrole").getAsRole();
        GuildChannelUnion resultsChan  = interaction.getOption("resultschan").getAsChannel();
        GuildChannelUnion leaderboard  = interaction.getOption("leaderboard").getAsChannel();

        // Sicherstellen dass die Kanäle Text-Kanäle sind
        if (!(resultsChan instanceof TextChannel) || !(leaderboard instanceof TextChannel)) {
            interaction.reply(MessageTemplates.ONLY_TEXT_CHANNELS)
                    .queue();
            return;
        }

        event.setAdminRoleId(adminRole.getId());
        event.setActivePlayerRoleId(playerRole.getId());
        event.setResultsChannelId(resultsChan.getId());
        event.setLiveboardChannelId(leaderboard.getId());
        event.setStatus(EventStatus.CONFIGURED);

        storage.saveEvent(event);

        interaction.reply(MessageTemplates.formatEventConfigured(
                event.getName(),
                adminRole.getAsMention(),
                playerRole.getAsMention(),
                resultsChan.getAsMention(),
                leaderboard.getAsMention()
        )).queue();
    }
}