package de.yolacraft.lcqbot.bot;

public final class MessageTemplates {

    private MessageTemplates() {
        // Utility class
    }

    public static final String UNKNOWN_COMMAND = "Unbekannter Command.";
    public static final String NO_ACTIVE_EVENT = "Kein aktives Event gefunden. Bitte Event-ID angeben oder zuerst /init ausführen.";
    public static final String EVENT_NOT_FOUND = "Event nicht gefunden.";
    public static final String NO_PERMISSION = "Du hast keine Berechtigung für diesen Command.";
    public static final String ONLY_TEXT_CHANNELS = "Bitte nur Text-Kanäle angeben.";
    public static final String EVENT_RUNNING = "Das Event läuft bereits — keine neuen Spieler möglich.";
    public static final String DISCORD_USER_NOT_FOUND = "Discord-User nicht gefunden.";
    public static final String EVENT_STARTED = "Event gestartet! Leaderboard wurde gesendet und Rollen wurden zugewiesen.";
    public static final String RESULT_CHANNEL_OR_ROLE_MISSING = "Ergebnis-Kanal oder Spieler-Rolle nicht gefunden. Bitte erst /config ausführen.";
    public static final String LEADERBOARD_TITLE = "Leaderboard";
    public static final String LEADERBOARD_DESCRIPTION = "Aktueller Punktestand:";
    public static final String OFFICIAL_RESULTS_TITLE = "Ergebnisse: Seed %d";
    public static final String LIVE_LEADERBOARD_TITLE = "Leaderboard %d/7 Seeds";
    public static final String LIVE_LEADERBOARD_FINISHED_TITLE = "Leaderboard %d/7 Seeds - Top 12";
    public static final String SEED_FINISHED = "**Seed %d** ist beendet! Bitte Ergebnisse prüfen und mit `/confirm_seed seed:%d` bestätigen.";
    public static final String SEED_NOT_FOUND = "Kein ausstehender Seed mit Nummer %d gefunden.";
    public static final String SEED_CONFIRMED = "Seed %d wurde bestätigt und Leaderboard geupdatet!";
    public static final String EVENT_CONFIGURED = "Event **%s** konfiguriert!\nAdmin-Rolle: %s\nSpieler-Rolle: %s\nErgebnisse: %s\nLeaderboard: %s";
    public static final String EVENT_CREATED = "Event **%s** erstellt!\nID: `%s`";
    public static final String PLAYER_ADDED = "Spieler hinzugefügt!\nIngame: `%s`\nDiscord: %s\nAlias: `%s`";
    public static final String PLAYER_LIST_HEADER = "**%d** Spieler sind Registriert:";
    public static final String PLAYER_COUNT = "**%d** Spieler sind Registriert";
    public static final String PLAYER_REMOVED = "%s wurde erfolgreich entfernt";
    public static final String PLAYER_NOT_FOUND = "%s ist nicht im Event registriert.";
    public static final String ALREADY_IN_EVENT = "%s ist bereits im Event.";
    public static final String TRACKING_STARTED = "Tracking für Seed **%d** gestartet. Warte auf Ergebnisse von Host: `%s`";

    public static String formatEventConfigured(String eventName, String adminMention, String playerMention, String resultsMention, String leaderboardMention) {
        return String.format(EVENT_CONFIGURED, eventName, adminMention, playerMention, resultsMention, leaderboardMention);
    }

    public static String formatEventCreated(String eventName, String eventId) {
        return String.format(EVENT_CREATED, eventName, eventId);
    }

    public static String formatPlayerAdded(String ingame, String discordMention, String alias) {
        return String.format(PLAYER_ADDED, ingame, discordMention, alias);
    }

    public static String formatAlreadyInEvent(String discordMention) {
        return String.format(ALREADY_IN_EVENT, discordMention);
    }

    public static String formatSeedNotFound(int seedNumber) {
        return String.format(SEED_NOT_FOUND, seedNumber);
    }

    public static String formatSeedConfirmed(int seedNumber) {
        return String.format(SEED_CONFIRMED, seedNumber);
    }

    public static String formatTrackingStarted(int seedNumber, String host) {
        return String.format(TRACKING_STARTED, seedNumber, host);
    }

    public static String formatSeedFinished(int seedNumber) {
        return String.format(SEED_FINISHED, seedNumber, seedNumber);
    }

    public static String formatOfficialResultsTitle(int seedNumber) {
        return String.format(OFFICIAL_RESULTS_TITLE, seedNumber);
    }

    public static String formatLiveLeaderboardTitle(int seedNumber) {
        return String.format(LIVE_LEADERBOARD_TITLE, seedNumber);
    }

    public static String formatLiveLeaderboardFinishedTitle(int seedNumber) {
        return String.format(LIVE_LEADERBOARD_FINISHED_TITLE, seedNumber);
    }
}