package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.EventResolver;
import de.yolacraft.lcqbot.bot.MessageTemplates;
import de.yolacraft.lcqbot.bot.RoleGuard;
import de.yolacraft.lcqbot.model.Event;
import de.yolacraft.lcqbot.model.EventStatus;
import de.yolacraft.lcqbot.model.Placement;
import de.yolacraft.lcqbot.model.Player;
import de.yolacraft.lcqbot.model.Seed;
import de.yolacraft.lcqbot.model.SeedStatus;
import de.yolacraft.lcqbot.bot.DiscordMessageSender;
import de.yolacraft.lcqbot.service.LeaderboardService;
import de.yolacraft.lcqbot.storage.FileStorageService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ConfirmSeedCommandHandler {

    private final FileStorageService storage;
    private final EventResolver resolver;
    private final RoleGuard roleGuard;
    private final LeaderboardService leaderboardService;

    public ConfirmSeedCommandHandler(FileStorageService storage, EventResolver resolver, RoleGuard roleGuard, LeaderboardService leaderboardService) {
        this.storage = storage;
        this.resolver = resolver;
        this.roleGuard = roleGuard;
        this.leaderboardService = leaderboardService;
    }

    public void handle(SlashCommandInteractionEvent interaction) {
        Optional<Event> opt = resolver.resolveAndReply(interaction);
        if (opt.isEmpty()) return;
        Event event = opt.get();

        if (!roleGuard.checkAndReply(interaction, event.getId())) return;

        int seedNum = interaction.getOption("seed").getAsInt();
        List<Seed> seeds = storage.loadSeeds(event.getId());

        Optional<Seed> targetSeed = seeds.stream()
                .filter(s -> s.getSeedNumber() == seedNum && s.getStatus() == SeedStatus.PENDING)
                .findFirst();

        if (targetSeed.isEmpty()) {
            interaction.reply(MessageTemplates.formatSeedNotFound(seedNum)).setEphemeral(true).queue();
            return;
        }

        Seed seed = targetSeed.get();
        seed.setStatus(SeedStatus.CONFIRMED);
        seed.setConfirmedAt(System.currentTimeMillis());
        storage.saveSeed(seed);

        // Ergebnisse formatieren und senden
        TextChannel resultsChannel = interaction.getGuild().getTextChannelById(event.getResultsChannelId());
        if (resultsChannel != null) {
            List<Player> allPlayers = storage.loadPlayers(event.getId());
            StringBuilder resultText = new StringBuilder();

            resultText.append("**").append(MessageTemplates.formatOfficialResultsTitle(seedNum)).append("**\n\n");

            seed.getPlacements().sort((p1, p2) -> Integer.compare(p1.getPlace(), p2.getPlace()));

            for (Placement pl : seed.getPlacements()) {
                String ingameName = allPlayers.stream()
                        .filter(p -> p.getId().equals(pl.getPlayerId()))
                        .map(Player::getIngameName)
                        .findFirst()
                        .orElse("Unknown");

                if (pl.isFinished()) {
                    long minutes = (pl.getFinishTimeMs() / 1000) / 60;
                    long seconds = (pl.getFinishTimeMs() / 1000) % 60;
                    resultText.append("**").append(pl.getPlace()).append(".** ").append(ingameName)
                            .append(" - `").append(minutes).append("m ").append(seconds).append("s`")
                            .append(" (+").append(pl.getPoints()).append(" Pkt)\n");
                } else {
                    resultText.append("❌ ").append(ingameName).append(" - DNF (0 Pkt)\n");
                }
            }

            DiscordMessageSender.sendSplitTextMessages(resultsChannel, resultText.toString(), ids -> {
                // Nur absenden, keine weiteren Aktionen erforderlich.
            });
        }

        // Leaderboard und Eliminierungen updaten
        leaderboardService.updateLeaderboardAndEliminate(event, seedNum);

        if (seedNum == 7) {
            event.setStatus(EventStatus.FINISHED);
            storage.saveEvent(event);
        }

        interaction.reply(MessageTemplates.formatSeedConfirmed(seedNum)).setEphemeral(true).queue();
    }
}