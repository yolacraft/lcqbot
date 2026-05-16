package de.yolacraft.lcqbot.bot;

import de.yolacraft.lcqbot.model.ReactionRole;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReactionRoleListener extends ListenerAdapter {

    private final FileStorageService storage;

    public ReactionRoleListener(FileStorageService storage) {
        this.storage = storage;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;

        Optional<ReactionRole> reactionRole = storage.findReactionRoleByMessageId(event.getMessageId());
        if (reactionRole.isEmpty()) return;

        ReactionRole config = reactionRole.get();
        if (!matchesEmoji(config.getEmoji(), event)) return;

        Guild guild = event.getGuild();
        Role role = guild.getRoleById(config.getRoleId());
        if (role == null) return;

        event.retrieveMember().queue(member -> assignRole(guild, member, role));
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getUserIdLong() == event.getJDA().getSelfUser().getIdLong()) return;

        Optional<ReactionRole> reactionRole = storage.findReactionRoleByMessageId(event.getMessageId());
        if (reactionRole.isEmpty()) return;

        ReactionRole config = reactionRole.get();
        if (!matchesEmoji(config.getEmoji(), event)) return;

        Guild guild = event.getGuild();
        Role role = guild.getRoleById(config.getRoleId());
        if (role == null) return;

        guild.retrieveMemberById(event.getUserId()).queue(member -> guild.removeRoleFromMember(member, role).queue(), failure -> {
        });
    }

    private boolean matchesEmoji(String storedEmoji, MessageReactionAddEvent event) {
        return event.getReaction().getEmoji().getFormatted().equals(storedEmoji);
    }

    private boolean matchesEmoji(String storedEmoji, MessageReactionRemoveEvent event) {
        return event.getReaction().getEmoji().getFormatted().equals(storedEmoji);
    }

    private void assignRole(Guild guild, Member member, Role role) {
        if (member.getRoles().stream().noneMatch(r -> r.getId().equals(role.getId()))) {
            guild.addRoleToMember(member, role).queue();
        }
    }
}
