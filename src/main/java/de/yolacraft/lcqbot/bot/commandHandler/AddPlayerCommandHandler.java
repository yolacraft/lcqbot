package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.EventResolver;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.EventStatus;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AddPlayerCommandHandler {

    private final FileStorageService storage;
    private final EventResolver resolver;
    private final RoleGuard roleGuard;

    public AddPlayerCommandHandler(
            FileStorageService storage,
            EventResolver resolver,
            RoleGuard roleGuard
    ) {
        this.storage = storage;
        this.resolver = resolver;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        Optional<Event> opt = resolver.resolveAndReply(interaction);
        if (opt.isEmpty()) return;

        Event event = opt.get();

        if (!roleGuard.checkAndReply(interaction, event.getId())) return;

        if (event.getStatus() == EventStatus.ACTIVE
                || event.getStatus() == EventStatus.TRACKING) {
            interaction.reply(MessageTemplates.EVENT_RUNNING)
                    .queue();
            return;
        }

        String ingame  = interaction.getOption("ingame").getAsString();
        String alias   = interaction.getOption("alias").getAsString();
        User user  = interaction.getOption("user").getAsUser();

        if (user == null) {
            interaction.reply(MessageTemplates.DISCORD_USER_NOT_FOUND)
                    .queue();
            return;
        }

        // Doppelter Spieler im selben Event verhindern
        boolean alreadyAdded = storage.loadPlayers(event.getId()).stream()
                .anyMatch(p -> p.getDiscordUserId().equals(user.getId()));

        if (alreadyAdded) {
            interaction.reply(MessageTemplates.formatAlreadyInEvent(user.getAsMention()))
                    .queue();
            return;
        }

        Player player = new Player();
        player.setId(UUID.randomUUID().toString());
        player.setEventId(event.getId());
        player.setIngameName(ingame);
        player.setDiscordName(user.getName());
        player.setAlias(alias);
        player.setDiscordUserId(user.getId());
        player.setEliminated(false);

        storage.savePlayer(player);

        interaction.reply(MessageTemplates.formatPlayerAdded(ingame, user.getAsMention(), alias)).queue();
    }
}
