package de.yolacraft.lcqbot.bot.commandHandler.player;

import de.yolacraft.lcqbot.bot.utils.MessageEncoder;
import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RemovePlayerCommandHandler {

    private final FileStorageService storage;
    private final RoleGuard roleGuard;

    public RemovePlayerCommandHandler(FileStorageService storage, RoleGuard roleGuard) {
        this.storage = storage;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) {
            return;
        }

        OptionMapping opt = interaction.getOption("event");
        String eventName = null;
        if (opt != null) {
            eventName = opt.getAsString();
        }

        User user = interaction.getOption("user").getAsUser();
        if (user == null) {
            interaction.reply(MessageEncoder.getMessage("discord_user_not_found")).queue();
            return;
        }

        List<Player> players = storage.getPlayers(eventName);
        Optional<Player> playerOpt = players.stream()
                .filter(p -> p.getDiscordUserId().equals(user.getId()))
                .findFirst();

        if (playerOpt.isEmpty()) {
            interaction.reply(MessageEncoder.format("player_not_found", user.getAsMention())).queue();
            return;
        }

        boolean removed = storage.removePlayer(eventName, user.getId());
        if (!removed) {
            interaction.reply(MessageEncoder.format("player_not_found", user.getAsMention())).queue();
            return;
        }

        interaction.reply(MessageEncoder.format("player_removed", user.getAsMention())).queue();
    }
}
