package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.EventResolver;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlayerCountCommandHandler {

    private final FileStorageService storage;
    private final EventResolver resolver;
    private final RoleGuard roleGuard;

    public PlayerCountCommandHandler(FileStorageService storage, EventResolver resolver, RoleGuard roleGuard) {
        this.storage = storage;
        this.resolver = resolver;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        Optional<Event> opt = resolver.resolveAndReply(interaction);
        if (opt.isEmpty()) {
            return;
        }

        Event event = opt.get();
        if (!roleGuard.checkAndReply(interaction, event.getId())) {
            return;
        }

        int count = storage.loadPlayers(event.getId()).size();
        interaction.reply(String.format(MessageTemplates.PLAYER_COUNT, count)).queue();
    }
}
