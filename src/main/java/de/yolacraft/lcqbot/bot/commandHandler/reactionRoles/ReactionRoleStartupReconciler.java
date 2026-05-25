package de.yolacraft.lcqbot.bot.commandHandler.reactionRoles;

import de.yolacraft.lcqbot.model.api.ReactionRole;
import de.yolacraft.lcqbot.storage.ReactionRoleFileService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReactionRoleStartupReconciler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactionRoleStartupReconciler.class);

    private final ReactionRoleFileService storage;
    private final JDA jda;

    public ReactionRoleStartupReconciler(ReactionRoleFileService storage, JDA jda) {
        this.storage = storage;
        this.jda = jda;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reconcileReactionRolesOnStartup() {
        LOGGER.info("Starting reaction-role reconciliation on startup");
        storage.loadReactionRoles().forEach(this::reconcileReactionRole);
    }

    private void reconcileReactionRole(ReactionRole config) {
        if (config.getGuildId() == null || config.getChannelId() == null || config.getMessageId() == null || config.getRoleId() == null || config.getEmoji() == null) {
            LOGGER.warn("Skipping incomplete reaction-role config: {}", config.getId());
            return;
        }

        Guild guild = jda.getGuildById(config.getGuildId());
        if (guild == null) {
            LOGGER.warn("Guild not found for reaction-role config {}: {}", config.getId(), config.getGuildId());
            return;
        }

        Role role = guild.getRoleById(config.getRoleId());
        if (role == null) {
            LOGGER.warn("Role not found for reaction-role config {}: {}", config.getId(), config.getRoleId());
            return;
        }

        TextChannel channel = guild.getTextChannelById(config.getChannelId());
        if (channel == null) {
            LOGGER.warn("Channel not found for reaction-role config {}: {}", config.getId(), config.getChannelId());
            return;
        }

        channel.retrieveMessageById(config.getMessageId()).queue(
                message -> reconcileMessageReactions(guild, role, config.getEmoji(), message),
                error -> LOGGER.warn("Could not retrieve reaction-role message for config {}: {}", config.getId(), error.getMessage())
        );
    }

    private void reconcileMessageReactions(Guild guild, Role role, String storedEmoji, Message message) {
        message.getReactions().stream()
                .filter(reaction -> matchesEmoji(storedEmoji, reaction))
                .findFirst()
                .ifPresent(reaction -> reaction.retrieveUsers().queue(
                        users -> syncRoleWithReactionUsers(guild, role, users),
                        error -> LOGGER.warn("Could not retrieve users for reaction {} on message {}: {}",
                                storedEmoji, message.getId(), error.getMessage())
                ));
    }

    private void syncRoleWithReactionUsers(Guild guild, Role role, List<User> users) {
        Set<String> reactingUserIds = users.stream()
                .filter(user -> !user.isBot())
                .map(User::getId)
                .collect(Collectors.toSet());

        reactingUserIds.forEach(userId -> guild.retrieveMemberById(userId).queue(
                member -> assignRole(guild, member, role),
                ignored -> {
                }
        ));

        guild.getMembersWithRoles(role).stream()
                .filter(member -> !reactingUserIds.contains(member.getId()))
                .forEach(member -> guild.removeRoleFromMember(member, role).queue());
    }

    private boolean matchesEmoji(String storedEmoji, MessageReaction reaction) {
        return reaction.getEmoji().getFormatted().equals(storedEmoji);
    }

    private void assignRole(Guild guild, Member member, Role role) {
        if (member.getRoles().stream().noneMatch(r -> r.getId().equals(role.getId()))) {
            guild.addRoleToMember(member, role).queue();
        }
    }
}
