package de.yolacraft.lcqbot.bot.commandHandler.event;

import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

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
        Role playerRole = interaction.getOption("player_role").getAsRole();
        Channel initChannel = interaction.getChannel();
        Channel leaderboardChannel = interaction.getOption("leaderboard_channel").getAsChannel();
        Channel logChannel = interaction.getOption("log_channel").getAsChannel();
        Channel resultsChannel = interaction.getOption("results_channel").getAsChannel();

        storage.createNewEvent(name, playerRole.getId(), initChannel.getId(), leaderboardChannel.getId(), logChannel.getId(), resultsChannel.getId());

        interaction.reply(String.format("Event `%s` konfiguriert!", name)).queue();
    }
}