package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.EventResolver;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RemovePlayerCommandHandler {

    private final FileStorageService storage;
    private final EventResolver resolver;
    private final RoleGuard roleGuard;

    public RemovePlayerCommandHandler(FileStorageService storage, EventResolver resolver, RoleGuard roleGuard) {
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

        User user = interaction.getOption("user").getAsUser();
        if (user == null) {
            interaction.reply(MessageTemplates.DISCORD_USER_NOT_FOUND).queue();
            return;
        }

        List<Player> players = storage.loadPlayers(event.getId());
        Optional<Player> playerOpt = players.stream()
                .filter(p -> p.getDiscordUserId().equals(user.getId()))
                .findFirst();

        if (playerOpt.isEmpty()) {
            interaction.reply(String.format(MessageTemplates.PLAYER_NOT_FOUND, user.getAsMention())).queue();
            return;
        }

        storage.deletePlayer(event.getId(), user.getId());
        interaction.reply(String.format(MessageTemplates.PLAYER_REMOVED, user.getAsMention())).queue();
    }
}
