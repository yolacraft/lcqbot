package de.yolacraft.lcqbot.bot;

import de.yolacraft.lcqbot.bot.commandHandler.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class CommandDispatcher extends ListenerAdapter {

    private final InitCommandHandler initHandler;
    private final ConfigCommandHandler configHandler;
    private final AddPlayerCommandHandler addPlayerHandler;
    private final StartEventCommandHandler startEventHandler;
    private final StartTrackingCommandHandler startTrackingHandler;
    private final ConfirmSeedCommandHandler confirmSeedCommandHandler;
    private final ListPlayersCommandHandler listPlayersCommandHandler;
    private final PlayerCountCommandHandler playerCountCommandHandler;
    private final RemovePlayerCommandHandler removePlayerCommandHandler;
    private final GlobalSeasonLeaderboardCommandHandler globalSeasonLeaderboardCommandHandler;
    private final AddPointsCommandHandler addPointsCommandHandler;
    private final SyncSeasonLeaderboardCommandHandler syncSeasonLeaderboardCommandHandler;
    private final LeaderboardCommandHandler leaderboardCommandHandler;
    private final ReactionRoleCommandHandler reactionRoleCommandHandler;
    private final TempSendHandler tempSendHandler;
    // weitere Handler kommen hier rein

    public CommandDispatcher(
            InitCommandHandler initHandler,
            ConfigCommandHandler configHandler,
            AddPlayerCommandHandler addPlayerHandler,
            StartEventCommandHandler startEventHandler,
            StartTrackingCommandHandler startTrackingHandler,
            ConfirmSeedCommandHandler confirmSeedCommandHandler,
            ListPlayersCommandHandler listPlayersCommandHandler,
            PlayerCountCommandHandler playerCountCommandHandler,
            RemovePlayerCommandHandler removePlayerCommandHandler,
            GlobalSeasonLeaderboardCommandHandler globalSeasonLeaderboardCommandHandler,
            AddPointsCommandHandler addPointsCommandHandler,
            SyncSeasonLeaderboardCommandHandler syncSeasonLeaderboardCommandHandler,
            LeaderboardCommandHandler leaderboardCommandHandler,
            ReactionRoleCommandHandler reactionRoleCommandHandler,
            TempSendHandler tempSendHandler
    ) {
        this.initHandler = initHandler;
        this.configHandler = configHandler;
        this.addPlayerHandler = addPlayerHandler;
        this.startEventHandler = startEventHandler;
        this.startTrackingHandler = startTrackingHandler;
        this.confirmSeedCommandHandler = confirmSeedCommandHandler;
        this.listPlayersCommandHandler = listPlayersCommandHandler;
        this.playerCountCommandHandler = playerCountCommandHandler;
        this.removePlayerCommandHandler = removePlayerCommandHandler;
        this.globalSeasonLeaderboardCommandHandler = globalSeasonLeaderboardCommandHandler;
        this.addPointsCommandHandler = addPointsCommandHandler;
        this.syncSeasonLeaderboardCommandHandler = syncSeasonLeaderboardCommandHandler;
        this.leaderboardCommandHandler = leaderboardCommandHandler;
        this.reactionRoleCommandHandler = reactionRoleCommandHandler;
        this.tempSendHandler = tempSendHandler;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "init"            -> initHandler.handle(event);
            case "config"          -> configHandler.handle(event);
            case "add_player"      -> addPlayerHandler.handle(event);
            case "list_players"    -> listPlayersCommandHandler.handle(event);
            case "player_count"    -> playerCountCommandHandler.handle(event);
            case "remove_player"   -> removePlayerCommandHandler.handle(event);
            case "start_event"     -> startEventHandler.handle(event);
            case "start_tracking"  -> startTrackingHandler.handle(event);
            case "confirm_seed"    -> confirmSeedCommandHandler.handle(event);
            case "global_create"   -> globalSeasonLeaderboardCommandHandler.handle(event);
            case "add_points"      -> addPointsCommandHandler.handle(event);
            case "sync"            -> syncSeasonLeaderboardCommandHandler.handle(event);
            case "leaderboard"     -> leaderboardCommandHandler.handle(event);
            case "role"            -> reactionRoleCommandHandler.handle(event);
            case "temp"            -> tempSendHandler.handle(event);


            default                -> event.reply(MessageTemplates.UNKNOWN_COMMAND).setEphemeral(true).queue();
        }
    }
}

