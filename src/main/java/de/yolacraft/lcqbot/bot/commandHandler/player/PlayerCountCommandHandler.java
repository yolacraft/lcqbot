package de.yolacraft.lcqbot.bot.commandHandler.player;

import de.yolacraft.lcqbot.bot.utils.MessageEncoder;
import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;


@Component
public class PlayerCountCommandHandler {

    private final FileStorageService storage;
    private final RoleGuard roleGuard;

    public PlayerCountCommandHandler(FileStorageService storage, RoleGuard roleGuard) {
        this.storage = storage;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        OptionMapping opt = interaction.getOption("event");
        String eventName = null;
        if(opt != null) eventName = opt.getAsString();

        int count = storage.getPlayers(eventName).size();
        interaction.reply(MessageEncoder.format("player_count", count)).queue();
    }
}
