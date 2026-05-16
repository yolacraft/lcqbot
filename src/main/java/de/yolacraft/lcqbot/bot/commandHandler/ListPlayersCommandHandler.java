package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.DiscordMessageSender;
import de.yolacraft.lcqbot.bot.EventResolver;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ListPlayersCommandHandler {

    private final FileStorageService storage;
    private final EventResolver resolver;
    private final RoleGuard roleGuard;

    public ListPlayersCommandHandler(FileStorageService storage, EventResolver resolver, RoleGuard roleGuard) {
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

        List<Player> players = storage.loadPlayers(event.getId());

        StringBuilder builder = new StringBuilder();
        builder.append(String.format(MessageTemplates.PLAYER_LIST_HEADER, players.size()));

        for (Player player : players) {
            builder.append("\n- ");
            builder.append("<@").append(player.getDiscordUserId()).append("> - ");
            builder.append(escapeDiscordMarkdown(player.getIngameName()));
        }

        String fullMessage = builder.toString();
        var chunks = DiscordMessageSender.splitMessage(fullMessage);

        if (chunks.size() <= 1) {
            interaction.reply(fullMessage).queue();
        } else {
            interaction.reply(chunks.get(0)).queue(ignored -> {
                for (int i = 1; i < chunks.size(); i++) {
                    interaction.getHook().sendMessage(chunks.get(i)).queue();
                }
            });
        }
    }

    /**
     * Escaped Discord Markdown Sonderzeichen, damit keine Formatierung passiert.
     */
    private String escapeDiscordMarkdown(String text) {
        if (text == null) return "";

        return text
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("~", "\\~")
                .replace("`", "\\`");
    }
}