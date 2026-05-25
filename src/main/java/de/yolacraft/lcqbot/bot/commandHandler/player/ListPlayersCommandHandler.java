package de.yolacraft.lcqbot.bot.commandHandler.player;

import de.yolacraft.lcqbot.bot.utils.DiscordMessageSender;
import de.yolacraft.lcqbot.bot.utils.MessageEncoder;
import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListPlayersCommandHandler {

    private final FileStorageService storage;
    private final RoleGuard roleGuard;

    public ListPlayersCommandHandler(FileStorageService storage, RoleGuard roleGuard) {
        this.storage = storage;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) return;

        OptionMapping opt = interaction.getOption("event");
        String eventName = null;
        if(opt != null) eventName = opt.getAsString();
        List<Player> players = storage.getPlayers(eventName);

        StringBuilder builder = new StringBuilder();
        builder.append(MessageEncoder.format("player_list_header", players.size()));

        for (Player player : players) {
            builder.append("\n- ");
            builder.append("<@").append(player.getDiscordUserId()).append("> - ");
            builder.append(escapeDiscordMarkdown(player.getIgn()));
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