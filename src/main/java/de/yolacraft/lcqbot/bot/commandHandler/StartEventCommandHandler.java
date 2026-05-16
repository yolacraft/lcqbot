package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.DiscordMessageSender;
import de.yolacraft.lcqbot.bot.EventResolver;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StartEventCommandHandler {

    private final FileStorageService storage;
    private final EventResolver resolver;
    private final RoleGuard roleGuard;

    public StartEventCommandHandler(FileStorageService storage,
                                    EventResolver resolver,
                                    RoleGuard roleGuard) {
        this.storage = storage;
        this.resolver = resolver;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {

        var opt = resolver.resolveAndReply(interaction);
        if (opt.isEmpty()) return;

        Event event = opt.get();

        if (!roleGuard.checkAndReply(interaction, event.getId())) return;

        var guild = interaction.getGuild();
        if (guild == null) return;

        TextChannel resultsChannel = guild.getTextChannelById(event.getResultsChannelId());
        Role eventRole = guild.getRoleById(event.getActivePlayerRoleId());

        if (resultsChannel == null || eventRole == null) {
            interaction.reply(MessageTemplates.RESULT_CHANNEL_OR_ROLE_MISSING).queue();
            return;
        }

        List<Player> players = storage.loadPlayers(event.getId());

        // -------------------------
        // LEADERBOARD
        // -------------------------
        StringBuilder leaderboardText = new StringBuilder();
        leaderboardText.append("**").append(MessageTemplates.LEADERBOARD_TITLE).append("**\n");
        leaderboardText.append(MessageTemplates.LEADERBOARD_DESCRIPTION).append("\n\n");

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            leaderboardText.append(i + 1)
                    .append(". ")
                    .append(p.getIngameName())
                    .append(" - **0** Punkte\n");
        }

        TextChannel liveboardChannel =
                guild.getTextChannelById(event.getLiveboardChannelId());

        TextChannel targetChannel =
                (liveboardChannel != null) ? liveboardChannel : resultsChannel;

        DiscordMessageSender.sendSplitTextMessages(
                targetChannel,
                leaderboardText.toString(),
                messageIds -> {
                    event.setLeaderboardMessageIds(messageIds);
                    storage.saveEvent(event);
                }
        );

        // -------------------------
        // ROLE ASSIGNMENT (FIXED + SAFE)
        // -------------------------

        List<Long> ids = players.stream()
                .map(p -> {
                    try {
                        return Long.parseLong(p.getDiscordUserId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(id -> id != null)
                .toList();

        guild.retrieveMembersByIds(ids).onSuccess(members -> {

            AtomicInteger delay = new AtomicInteger(0);

            for (Member member : members) {
                guild.addRoleToMember(member, eventRole)
                        .queueAfter(delay.getAndAdd(250), TimeUnit.MILLISECONDS);
            }

        });

        interaction.reply(MessageTemplates.EVENT_STARTED).queue();
    }
}