package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.EventResolver;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.service.LeaderboardService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LeaderboardCommandHandler {

    private final LeaderboardService leaderboardService;
    private final EventResolver resolver;
    private final RoleGuard roleGuard;

    public LeaderboardCommandHandler(LeaderboardService leaderboardService, EventResolver resolver, RoleGuard roleGuard) {
        this.leaderboardService = leaderboardService;
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

        // Leaderboard aktualisieren ohne Eliminierung
        leaderboardService.updateLeaderboardOnly(event);

        interaction.reply("Leaderboard wurde aktualisiert.").setEphemeral(true).queue();
    }
}