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
                        .addOption(OptionType.STRING, "name", "Name des Events", true)
                        .addOption(OptionType.STRING, "apikey", "API Key für das Tracking", true),

                Commands.slash("config", "Konfiguriert Rollen und Kanäle für ein Event")
                        .addOption(OptionType.ROLE,    "adminrole",    "Rolle die Commands ausführen darf", true)
                        .addOption(OptionType.ROLE,    "playerrole",   "Rolle für aktive Spieler", true)
                        .addOption(OptionType.CHANNEL, "resultschan",  "Kanal für Ergebnisse", true)
                        .addOption(OptionType.CHANNEL, "leaderboard",  "Kanal für Live-Leaderboard", true)
                        .addOption(OptionType.STRING,  "event",        "Event ID (optional, sonst latest)", false),

                Commands.slash("add_player", "Fügt einen Spieler zum Event hinzu")
                        .addOption(OptionType.STRING, "ingame",   "Ingame Name", true)
                        .addOption(OptionType.STRING, "alias",    "Alias/Spitzname", true)
                        .addOption(OptionType.USER,   "user",     "Discord User", true)
                        .addOption(OptionType.STRING, "event",    "Event ID (optional)", false),

                Commands.slash("list_players", "Listet alle Spieler für ein Event")
                        .addOption(OptionType.STRING, "event", "Event ID (optional, sonst latest)", false),

                Commands.slash("player_count", "Zeigt die Anzahl der Spieler im Event")
                        .addOption(OptionType.STRING, "event", "Event ID (optional, sonst latest)", false),

                Commands.slash("remove_player", "Entfernt einen Spieler aus dem Event")
                        .addOption(OptionType.USER, "user", "Discord User", true)
                        .addOption(OptionType.STRING, "event", "Event ID (optional, sonst latest)", false),

                Commands.slash("start_event", "Gibt allen Spielern die Spieler-Rolle")
                        .addOption(OptionType.STRING, "event", "Event ID (optional)", false),

                Commands.slash("start_tracking", "Startet das Match-Tracking über die API")
                        .addOption(OptionType.STRING, "event", "Event ID (optional)", false)
                        .addOption(OptionType.STRING, "host", "Player der den Room hostet"),

                Commands.slash("confirm_seed", "Bestätigt das Ergebnis des letzten Seeds")
                        .addOption(OptionType.INTEGER, "seed",  "Seed Nummer", true)
                        .addOption(OptionType.STRING,  "event", "Event ID (optional)", false),

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

                Commands.slash("leaderboard", "Aktualisiert das Live-Leaderboard für ein Event")
                        .addOption(OptionType.STRING, "event", "Event ID (optional, sonst latest)", false),

                Commands.slash("role", "Erstellt eine Reaction-Role Nachricht")
                        .addOption(OptionType.ROLE,   "role", "Rolle, die bei Reaction vergeben wird", true)
                        .addOption(OptionType.STRING, "reaction", "Emoji für die Reaction", true),

                Commands.slash("temp", "temp")
        ).queue();
    }
}