package de.yolacraft.lcqbot.bot.commandHandler.debug;

import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

@Component
public class ParseDataCommandHandler {
    private final FileStorageService storage;
    private final RoleGuard roleGuard;

    public ParseDataCommandHandler(FileStorageService storage, RoleGuard roleGuard) {
        this.storage = storage;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) return;

        OptionMapping opt = interaction.getOption("event");
        String eventName = null;
        if(opt != null) eventName = opt.getAsString();

        String file  = interaction.getOption("file").getAsString();
        int id  = interaction.getOption("id").getAsInt();

        storage.parseRawData(file, id, eventName);

        interaction.reply("done").queue();
    }
}
