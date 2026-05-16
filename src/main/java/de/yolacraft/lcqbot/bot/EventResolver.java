package de.yolacraft.lcqbot.bot;

import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EventResolver {

    private final FileStorageService storage;

    public EventResolver(FileStorageService storage) {
        this.storage = storage;
    }

    public Optional<Event> resolve(SlashCommandInteractionEvent interaction) {
        OptionMapping opt = interaction.getOption("event");

        if (opt != null) {
            String id = opt.getAsString();
            return storage.loadAllEvents().stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst();
        }

        return storage.findLatestEvent();
    }

    public Optional<Event> resolveAndReply(SlashCommandInteractionEvent interaction) {
        Optional<Event> ev = resolve(interaction);
        if (ev.isEmpty()) {
            interaction.reply(MessageTemplates.NO_ACTIVE_EVENT)
                    .setEphemeral(true).queue();
        }
        return ev;
    }
}
