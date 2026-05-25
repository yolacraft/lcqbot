package de.yolacraft.lcqbot.bot;

import de.yolacraft.lcqbot.bot.commandHandler.*;
import de.yolacraft.lcqbot.bot.commandHandler.debug.ParseDataCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.event.InitCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.globalLeaderboard.AddPointsCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.globalLeaderboard.GlobalSeasonLeaderboardCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.globalLeaderboard.SyncSeasonLeaderboardCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.player.AddPlayerCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.player.ListPlayersCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.player.PlayerCountCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.player.RemovePlayerCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.reactionRoles.ReactionRoleCommandHandler;
import de.yolacraft.lcqbot.bot.commandHandler.register.RegisterCommandHandler;
import de.yolacraft.lcqbot.bot.utils.MessageEncoder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class CommandDispatcher extends ListenerAdapter {

    private final InitCommandHandler initHandler;
    private final AddPlayerCommandHandler addPlayerHandler;
    private final StartTrackingCommandHandler startTrackingHandler;
    private final StartEventCommandHandler startEventCommandHandler;
    private final ListPlayersCommandHandler listPlayersCommandHandler;
    private final PlayerCountCommandHandler playerCountCommandHandler;
    private final RemovePlayerCommandHandler removePlayerCommandHandler;
    private final GlobalSeasonLeaderboardCommandHandler globalSeasonLeaderboardCommandHandler;
    private final AddPointsCommandHandler addPointsCommandHandler;
    private final SyncSeasonLeaderboardCommandHandler syncSeasonLeaderboardCommandHandler;
    private final LeaderboardSyncCommandHandler leaderboardSyncCommandHandler;
    private final ReactionRoleCommandHandler reactionRoleCommandHandler;
    private final RegisterCommandHandler registerCommandHandler;
    private final ParseDataCommandHandler parseDataCommandHandler;
    private final InsertSeedCommandHandler insertSeedCommandHandler;
    // weitere Handler kommen hier rein

    public CommandDispatcher(
            InitCommandHandler initHandler,
            AddPlayerCommandHandler addPlayerHandler,
            StartTrackingCommandHandler startTrackingHandler,
            StartEventCommandHandler startEventCommandHandler,
            ListPlayersCommandHandler listPlayersCommandHandler,
            PlayerCountCommandHandler playerCountCommandHandler,
            RemovePlayerCommandHandler removePlayerCommandHandler,
            GlobalSeasonLeaderboardCommandHandler globalSeasonLeaderboardCommandHandler,
            AddPointsCommandHandler addPointsCommandHandler,
            SyncSeasonLeaderboardCommandHandler syncSeasonLeaderboardCommandHandler,
            LeaderboardSyncCommandHandler leaderboardSyncCommandHandler,
            ReactionRoleCommandHandler reactionRoleCommandHandler,
            RegisterCommandHandler registerCommandHandler,
            ParseDataCommandHandler parseDataCommandHandler,
            InsertSeedCommandHandler insertSeedCommandHandler
    ) {
        this.initHandler = initHandler;
        this.addPlayerHandler = addPlayerHandler;
        this.startTrackingHandler = startTrackingHandler;
        this.startEventCommandHandler = startEventCommandHandler;
        this.listPlayersCommandHandler = listPlayersCommandHandler;
        this.playerCountCommandHandler = playerCountCommandHandler;
        this.removePlayerCommandHandler = removePlayerCommandHandler;
        this.globalSeasonLeaderboardCommandHandler = globalSeasonLeaderboardCommandHandler;
        this.addPointsCommandHandler = addPointsCommandHandler;
        this.syncSeasonLeaderboardCommandHandler = syncSeasonLeaderboardCommandHandler;
        this.leaderboardSyncCommandHandler = leaderboardSyncCommandHandler;
        this.reactionRoleCommandHandler = reactionRoleCommandHandler;
        this.registerCommandHandler = registerCommandHandler;
        this.parseDataCommandHandler = parseDataCommandHandler;
        this.insertSeedCommandHandler = insertSeedCommandHandler;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "init"            -> initHandler.handle(event);
            case "add_player"      -> addPlayerHandler.handle(event);
            case "list_players"    -> listPlayersCommandHandler.handle(event);
            case "player_count"    -> playerCountCommandHandler.handle(event);
            case "remove_player"   -> removePlayerCommandHandler.handle(event);
            case "start_tracking"  -> startTrackingHandler.handle(event);
            case "start_event"     -> startEventCommandHandler.handle(event);
            case "global_create"   -> globalSeasonLeaderboardCommandHandler.handle(event);
            case "add_points"      -> addPointsCommandHandler.handle(event);
            case "sync"            -> syncSeasonLeaderboardCommandHandler.handle(event);
            case "leaderboard_sync" -> leaderboardSyncCommandHandler.handle(event);
            case "role"            -> reactionRoleCommandHandler.handle(event);
            case "parse_data"     -> parseDataCommandHandler.handle(event);
            case "insert_seed"     -> insertSeedCommandHandler.handle(event);
            case "register"        -> {
                try {
                    registerCommandHandler.handle(event);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            default                -> event.reply(MessageEncoder.getMessage("unknown_command")).setEphemeral(true).queue();
        }
    }
}

