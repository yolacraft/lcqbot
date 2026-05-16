package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.seasonleaderboard.SeasonLeaderboard;
import de.yolacraft.lcqbot.seasonleaderboard.SeasonLeaderboardException;
import de.yolacraft.lcqbot.seasonleaderboard.SeasonLeaderboardService;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class GlobalSeasonLeaderboardCommandHandler {

    private final SeasonLeaderboardService leaderboardService;

    public GlobalSeasonLeaderboardCommandHandler(SeasonLeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        interaction.deferReply(true).queue(hook -> {
            int season = interaction.getOption("season").getAsInt();
            int week = interaction.getOption("week").getAsInt();
            var channelOption = interaction.getOption("channel").getAsChannel();

            if (!(channelOption instanceof MessageChannel targetChannel)) {
                hook.sendMessage("The provided channel must be a text channel.").queue();
                return;
            }

            SeasonLeaderboard leaderboard;
            try {
                leaderboard = leaderboardService.createEmptyLeaderboard(season, week, targetChannel.getId());
            } catch (SeasonLeaderboardException e) {
                hook.sendMessage(e.getMessage()).queue();
                return;
            }

            targetChannel.sendMessage(leaderboardService.buildText(leaderboard)).queue(sentMessage -> {
                leaderboard.setMessageId(sentMessage.getId());
                leaderboardService.saveLeaderboard(leaderboard);
                leaderboardService.saveLatestLeaderboardId(leaderboard.getId());
                hook.sendMessage("Leaderboard created: " + leaderboard.getId()).queue();
            }, failure -> hook.sendMessage("Failed to send leaderboard message: " + failure.getMessage()).queue());
        });
    }
}
