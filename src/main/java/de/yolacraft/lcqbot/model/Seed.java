package de.yolacraft.lcqbot.model;

import java.util.Map;
import java.util.UUID;

public class Seed {
    private UUID uuid;
    private int seedNumber;
    private long startedAt;
    private Map<UUID, Long> completions;

    //SEED INFO
    private String seedType;
    private String bastionType;
    private int[] endTowers;
    private String[] variations;

    public Seed(UUID uuid, int seedNumber, long startedAt, Map<UUID, Long> completions, String seedType, String bastionType, int[] endTowers, String[] variations) {
        this.uuid = uuid;
        this.seedNumber = seedNumber;
        this.startedAt = startedAt;
        this.completions = completions;
        this.seedType = seedType;
        this.bastionType = bastionType;
        this.endTowers = endTowers;
        this.variations = variations;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getSeedNumber() {
        return seedNumber;
    }

    public void setSeedNumber(int seedNumber) {
        this.seedNumber = seedNumber;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public Map<UUID, Long> getCompletions() {
        return completions;
    }

    public void setCompletions(Map<UUID, Long> completions) {
        this.completions = completions;
    }

    public String getSeedType() {
        return seedType;
    }

    public void setSeedType(String seedType) {
        this.seedType = seedType;
    }

    public String getBastionType() {
        return bastionType;
    }

    public void setBastionType(String bastionType) {
        this.bastionType = bastionType;
    }

    public int[] getEndTowers() {
        return endTowers;
    }

    public void setEndTowers(int[] endTowers) {
        this.endTowers = endTowers;
    }

    public String[] getVariations() {
        return variations;
    }

    public void setVariations(String[] variations) {
        this.variations = variations;
    }
}
