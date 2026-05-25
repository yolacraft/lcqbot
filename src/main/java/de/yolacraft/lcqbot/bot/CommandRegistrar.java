package de.yolacraft.lcqbot.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CommandRegistrar implements ApplicationRunner {

    private final JDA jda;

    public CommandRegistrar(@Lazy JDA jda) {
        this.jda = jda;
    }

    @Override
    public void run(ApplicationArguments args) {
        jda.updateCommands().addCommands(
                Commands.slash("init", "Erstellt ein neues Event")
                        .addOption(OptionType.STRING, "name", "name of the event", true)
                        .addOption(OptionType.ROLE, "player_role", "Role for the players while in tournament", true)
                        .addOption(OptionType.CHANNEL, "leaderboard_channel", "Channel for the live Leaderboard", true)
                        .addOption(OptionType.CHANNEL, "log_channel", "Channel for the live Logs", true)
                        .addOption(OptionType.CHANNEL, "results_channel", "Channel for the match Results", true),

                Commands.slash("add_player", "Fügt einen Spieler zum Event hinzu")
                        .addOption(OptionType.STRING, "uuid",   "mc uuid", true)
                        .addOption(OptionType.STRING, "ingame",   "ingame name", true)
                        .addOption(OptionType.USER,   "user",     "Discord User", true)
                        .addOption(OptionType.STRING,   "twitch",     "Twitch Username", true)
                        .addOption(OptionType.STRING, "event",    "Event Name (optional)", false),

                Commands.slash("register", "Registriere dich für WEEKLY SUMMIT")
                        .addOption(OptionType.STRING, "ign", "Dein UserName in MCSR RANKED", true)
                        .addOption(OptionType.STRING, "twitch", "Dein Twitch Username", true)
                        .addOption(OptionType.USER, "user", "Discord User (optional, sonst du selbst)", false),

                Commands.slash("list_players", "Listet alle Spieler für ein Event")
                        .addOption(OptionType.STRING, "event", "Event ID (optional, sonst latest)", false),

                Commands.slash("player_count", "Zeigt die Anzahl der Spieler im Event")
                        .addOption(OptionType.STRING, "event", "Event ID (optional, sonst latest)", false),

                Commands.slash("remove_player", "Entfernt einen Spieler aus dem Event")
                        .addOption(OptionType.USER, "user", "Discord User", true)
                        .addOption(OptionType.STRING, "event", "Event ID (optional, sonst latest)", false),

                Commands.slash("start_tracking", "Startet das Match-Tracking über die API")
                        .addOption(OptionType.STRING, "event", "Event ID (optional)", false)
                        .addOption(OptionType.STRING, "host", "Player der den Room hostet"),

                Commands.slash("start_event", "Startet ein Event - gibt Rollen und zeigt Leaderboard")
                        .addOption(OptionType.STRING, "event", "Event Name (optional, sonst latest)", false),

                Commands.slash("global_create", "Erstellt ein globales Season Leaderboard")
                        .addOption(OptionType.INTEGER, "season", "Season Nummer", true)
                        .addOption(OptionType.INTEGER, "week", "Woche 1-4", true)
                        .addOption(OptionType.CHANNEL, "channel", "Kanal für das Leaderboard", true),

                Commands.slash("add_points", "Fügt Punkte zu einem Season Leaderboard hinzu")
                        .addOption(OptionType.STRING, "player", "Spielername", true)
                        .addOption(OptionType.INTEGER, "week", "Woche 1-4", true)
                        .addOption(OptionType.INTEGER, "points", "Punktewert", true)
                        .addOption(OptionType.STRING, "leaderboard", "Leaderboard UUID (optional)", false),

                Commands.slash("sync", "Synchronisiert das gespeicherte Season Leaderboard")
                        .addOption(OptionType.STRING, "leaderboard", "Leaderboard UUID (optional)", false),

                Commands.slash("role", "Erstellt eine Reaction-Role Nachricht")
                        .addOption(OptionType.ROLE,   "role", "Rolle, die bei Reaction vergeben wird", true)
                        .addOption(OptionType.STRING, "reaction", "Emoji für die Reaction", true),

                Commands.slash("parse_data", "parse raw seed data")
                        .addOption(OptionType.STRING,   "file", "filename", true)
                        .addOption(OptionType.INTEGER, "id", "seedidentifyer", true)
                        .addOption(OptionType.STRING,  "event", "Event ID (optional)", false),

                Commands.slash("insert_seed", "insert a seed to database")
                        .addOption(OptionType.INTEGER, "id", "matchid", true)
                        .addOption(OptionType.INTEGER, "seednumber", "seednumber", true)
                        .addOption(OptionType.STRING,  "event", "Event ID (optional)", false)
                ,
                Commands.slash("leaderboard_sync", "Aktualisiert nur das Leaderboard")
                        .addOption(OptionType.STRING, "event", "Event Name (optional, sonst latest)", false)
        ).queue();
    }
}