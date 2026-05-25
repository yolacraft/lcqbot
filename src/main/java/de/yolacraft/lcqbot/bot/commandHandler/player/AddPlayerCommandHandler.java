package de.yolacraft.lcqbot.bot.commandHandler.player;

import de.yolacraft.lcqbot.bot.utils.RoleGuard;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

@Component
public class AddPlayerCommandHandler {

    private final FileStorageService storage;
    private final RoleGuard roleGuard;

    public AddPlayerCommandHandler(
            FileStorageService storage,
            RoleGuard roleGuard
    ) {
        this.storage = storage;
        this.roleGuard = roleGuard;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        if (!roleGuard.checkAdminOrFixedRoleAndReply(interaction)) return;

        String ingame  = interaction.getOption("ingame").getAsString();
        String uuid  = interaction.getOption("uuid").getAsString();
        User user  = interaction.getOption("user").getAsUser();
        String twitch = interaction.getOption("twitch").getAsString();

        OptionMapping opt = interaction.getOption("event");
        String eventName = null;
        if(opt != null) eventName = opt.getAsString();

        Player player = new Player(user.getId(), uuid, twitch, ingame);
        storage.addPlayer(player, eventName, user.getName());

        interaction.reply(String.format("Spieler erfolgreich hinzugefügt:\nMC_UUID: `%s`\nDC_UUID: `%s`", ingame, user.getId())).queue();
    }
}
