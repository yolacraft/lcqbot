package de.yolacraft.lcqbot.bot;

import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleGuard {

    private static final String ALLOWED_ROLE_ID = "1076284365652361257";

    private final FileStorageService storage;

    public RoleGuard(FileStorageService storage) {
        this.storage = storage;
    }

    /**
     * Gibt true zurück wenn der Member die Admin-Rolle des Events hat.
     * Antwortet selbst mit einer Fehlermeldung wenn nicht berechtigt.
     */
    public boolean checkAndReply(SlashCommandInteractionEvent event, String eventId) {
        Optional<Event> ev = storage.loadAllEvents().stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst();

        if (ev.isEmpty()) {
            event.reply(MessageTemplates.EVENT_NOT_FOUND).setEphemeral(true).queue();
            return false;
        }

        String adminRoleId = ev.get().getAdminRoleId();
        return checkMemberAuth(event, adminRoleId);
    }

    public boolean checkAdminOrFixedRoleAndReply(SlashCommandInteractionEvent event) {
        return checkMemberAuth(event, null);
    }

    private boolean checkMemberAuth(SlashCommandInteractionEvent event, String adminRoleId) {
        if (event.getMember() == null) {
            event.reply(MessageTemplates.NO_PERMISSION).setEphemeral(true).queue();
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

        event.reply(MessageTemplates.NO_PERMISSION)
                .setEphemeral(true)
                .queue();
        return false;
    }
}