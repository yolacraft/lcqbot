package de.yolacraft.lcqbot.model;

public class Player {
    private String id;
    private String eventId;
    private String ingameName;
    private String discordName;
    private String alias;
    private String discordUserId;   // Discord Snowflake ID
    private boolean eliminated = false;

    public Player() {}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getIngameName() {
        return ingameName;
    }

    public void setIngameName(String ingameName) {
        this.ingameName = ingameName;
    }

    public String getDiscordName() {
        return discordName;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(String discordUserId) {
        this.discordUserId = discordUserId;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
}
