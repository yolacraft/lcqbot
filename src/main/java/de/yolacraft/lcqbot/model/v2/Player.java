package de.yolacraft.lcqbot.model.v2;

import java.util.UUID;

public class Player {
    private UUID uuid;
    private String discordUserId;
    private String minecraftUUID;

    public Player(String discordUserId, String minecraftUUID) {
        this.discordUserId = discordUserId;
        this.minecraftUUID = minecraftUUID;
        uuid = UUID.fromString(minecraftUUID);
    }
}
