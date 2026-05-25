package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.service.LeaderboardService;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

@Component
public class LeaderboardSyncCommandHandler {

    private final RoleGuard roleGuard;
    private final FileStorageService storage;
    private final LeaderboardService leaderboardService;

    public LeaderboardSyncCommandHandler(RoleGuard roleGuard, FileStorageService storage, LeaderboardService leaderboardService) {
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
                hook.sendMessage("Error retrieving event: " + e.getMessage()).queue();
                return;
            }

            leaderboardService.updateLeaderboardOnly(event);
            hook.sendMessage("Leaderboard updated.").queue();
        });
    }
}
