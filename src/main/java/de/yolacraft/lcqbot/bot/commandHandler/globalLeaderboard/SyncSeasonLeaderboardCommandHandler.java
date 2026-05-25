package de.yolacraft.lcqbot.bot.commandHandler.globalLeaderboard;

import de.yolacraft.lcqbot.seasonleaderboard.SeasonLeaderboard;
import de.yolacraft.lcqbot.seasonleaderboard.SeasonLeaderboardException;
import de.yolacraft.lcqbot.seasonleaderboard.SeasonLeaderboardService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class SyncSeasonLeaderboardCommandHandler {

    private final SeasonLeaderboardService leaderboardService;

    public SyncSeasonLeaderboardCommandHandler(SeasonLeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        interaction.deferReply(true).queue(hook -> {
            var leaderboardOption = interaction.getOption("leaderboard");
            String leaderboardId = leaderboardOption != null ? leaderboardOption.getAsString() : null;

            SeasonLeaderboard leaderboard;
            try {
                leaderboard = leaderboardService.loadLeaderboardOrLatest(leaderboardId);
            } catch (SeasonLeaderboardException e) {
                hook.sendMessage(e.getMessage()).queue();
                return;
            }

            if (leaderboard.getChannelId() == null || leaderboard.getMessageId() == null) {
                hook.sendMessage("The leaderboard metadata is missing stored channel or message IDs.").queue();
                return;
            }

            TextChannel channel = interaction.getJDA().getTextChannelById(leaderboard.getChannelId());
            if (channel == null) {
                hook.sendMessage("The leaderboard channel no longer exists.").queue();
                return;
            }

            channel.retrieveMessageById(leaderboard.getMessageId()).queue(storedMessage -> {
                storedMessage.editMessage(leaderboardService.buildText(leaderboard)).queue(
                        success -> hook.sendMessage("Leaderboard synchronized successfully.").queue(),
                        failure -> hook.sendMessage("Failed to edit leaderboard message: " + failure.getMessage()).queue()
                );
            }, failure -> hook.sendMessage("Stored leaderboard message was not found.").queue());
        });
    }
}
