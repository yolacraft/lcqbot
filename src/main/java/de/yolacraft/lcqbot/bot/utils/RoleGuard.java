package de.yolacraft.lcqbot.bot.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleGuard {

    private static final String ALLOWED_ROLE_ID = "1076284365652361257";


    public RoleGuard() {
    }

    public boolean checkAdminOrFixedRoleAndReply(SlashCommandInteractionEvent event) {
        return checkMemberAuth(event, null);
    }

    private boolean checkMemberAuth(SlashCommandInteractionEvent event, String adminRoleId) {
        if (event.getMember() == null) {
            event.reply(MessageEncoder.getMessage("no_permission")).setEphemeral(true).queue();
            return false;
        }

        boolean isAdmin = event.getMember().hasPermission(Permission.ADMINISTRATOR);
        boolean hasFixedRole = event.getMember().getRoles().stream()
                .anyMatch(r -> r.getId().equals(ALLOWED_ROLE_ID));
        boolean hasConfiguredRole = adminRoleId != null && event.getMember().getRoles().stream()
                .anyMatch(r -> r.getId().equals(adminRoleId));

        if (isAdmin || hasFixedRole || hasConfiguredRole) {
            return true;
        }

        event.reply(MessageEncoder.getMessage("no_permission"))
                .setEphemeral(true)
                .queue();
        return false;
    }
}