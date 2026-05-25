package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.service.LeaderboardService;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StartEventCommandHandler {

    private final RoleGuard roleGuard;
    private final FileStorageService storage;
    private final LeaderboardService leaderboardService;

    public StartEventCommandHandler(RoleGuard roleGuard, FileStorageService storage, LeaderboardService leaderboardService) {
        this.roleGuard = roleGuard;
        this.storage = storage;
        this.leaderboardService = leaderboardService;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) return;

        OptionMapping opt = interaction.getOption("event");
        String eventName = null;
        if (opt != null) eventName = opt.getAsString();
        
        final String finalEventName = eventName;

        interaction.deferReply(true).queue(hook -> {
            Event event;
            try {
                event = storage.getEvent(finalEventName);
            } catch (IllegalStateException e) {
                hook.sendMessage("Event nicht gefunden: " + e.getMessage()).queue();
                return;
            } catch (Exception e) {
                hook.sendMessage("Error starting event: " + e.getMessage()).queue();
                return;
            }

            // Get guild and role
            Guild guild = interaction.getGuild();
            if (guild == null) {
                hook.sendMessage("Could not get guild").queue();
                return;
            }

            Role playerRole = guild.getRoleById(event.getPlayerRoleId());
            if (playerRole == null) {
                hook.sendMessage("Player role not found for this event").queue();
                return;
            }

            // Assign role to all players
List<Player> players = storage.getPlayers(event.getUuid().toString());
            int assignedCount = 0;

            for (Player player : players) {
                guild.retrieveMemberById(player.getDiscordUserId()).queue(
                        member -> guild.addRoleToMember(member, playerRole).queue(
                                success -> {},
                                error -> System.err.println("Failed to assign role to " + player.getIgn())
                        ),
                        error -> System.err.println("Could not retrieve member: " + player.getDiscordUserId())
                );
                assignedCount++;
            }

            // Send leaderboard
            leaderboardService.updateLeaderboardOnly(event);

            hook.sendMessage("Event started! Assigned roles to " + assignedCount + " players and sent leaderboard.").queue();
        });
    }
}
