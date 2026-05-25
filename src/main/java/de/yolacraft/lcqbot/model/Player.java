package de.yolacraft.lcqbot.model;

import java.util.UUID;

public class Player {
    private UUID uuid;
    private String discordUserId;
    private String minecraftUUID;
    private String twitchUserName;
    private String ign;
    private boolean eliminated;

    public Player(String discordUserId, String minecraftUUID, String twitchUserName, String ign) {
        this.discordUserId = discordUserId;
        this.minecraftUUID = minecraftUUID;
        uuid = UUID.randomUUID();
        this.twitchUserName = twitchUserName;
        this.ign = ign;
        eliminated = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(String discordUserId) {
        this.discordUserId = discordUserId;
    }

    public String getMinecraftUUID() {
        return minecraftUUID;
    }

    public void setMinecraftUUID(String minecraftUUID) {
        this.minecraftUUID = minecraftUUID;
    }

    public String getTwitchUserName() {
        return twitchUserName;
    }

    public void setTwitchUserName(String twitchUserName) {
        this.twitchUserName = twitchUserName;
    }

    public String getIgn() {
        return ign;
    }

    public void setIgn(String ign) {
        this.ign = ign;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
}
