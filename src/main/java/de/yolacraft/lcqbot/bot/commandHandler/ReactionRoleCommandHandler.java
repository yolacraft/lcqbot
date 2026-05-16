package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.ReactionRole;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReactionRoleCommandHandler {

    private final FileStorageService storage;
    private final RoleGuard roleGuard;

    public ReactionRoleCommandHandler(FileStorageService storage, RoleGuard roleGuard) {
        this.storage = storage;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) return;

        if (!interaction.isFromGuild()) {
            interaction.reply("Dieser Command kann nur in einem Server ausgeführt werden.").setEphemeral(true).queue();
            return;
        }

        Role role = interaction.getOption("role").getAsRole();
        String emoji = interaction.getOption("reaction").getAsString();

        if (role == null) {
            interaction.reply("Rolle nicht gefunden.").setEphemeral(true).queue();
            return;
        }

        if (emoji == null || emoji.isBlank()) {
            interaction.reply("Bitte ein gültiges Emoji angeben.").setEphemeral(true).queue();
            return;
        }

        Emoji parsedEmoji;
        try {
            parsedEmoji = Emoji.fromFormatted(emoji.trim());
        } catch (IllegalArgumentException ex) {
            interaction.reply("Das eingegebene Emoji ist ungültig. Bitte Unicode-Emoji oder ein Custom-Emoji im Format <name:id> verwenden.").setEphemeral(true).queue();
            return;
        }

        String messageContent = "Reagiere mit " + parsedEmoji.getFormatted() + " um die Rolle " + role.getAsMention() + " zu erhalten.";

        interaction.reply(messageContent).setEphemeral(false).queue(hook -> {
            hook.retrieveOriginal().queue(sentMessage -> {
                sentMessage.addReaction(parsedEmoji).queue(
                        ignored -> {
                            ReactionRole reactionRole = new ReactionRole();
                            reactionRole.setId(UUID.randomUUID().toString());
                            reactionRole.setGuildId(interaction.getGuild().getId());
                            reactionRole.setChannelId(interaction.getChannel().getId());
                            reactionRole.setMessageId(sentMessage.getId());
                            reactionRole.setRoleId(role.getId());
                            reactionRole.setEmoji(parsedEmoji.getFormatted());
                            reactionRole.setText(messageContent);
                            storage.saveReactionRole(reactionRole);
                        }, failure -> {
                            sentMessage.delete().queue();
                            hook.sendMessage("Das angegebene Emoji konnte nicht zur Nachricht hinzugefügt werden. Bitte überprüfe das Emoji.").setEphemeral(true).queue();
                        }
                );
            });
        });
    }
}
