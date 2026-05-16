package de.yolacraft.lcqbot.model;

import java.util.ArrayList;
import java.util.List;

public class Seed {
    private String id;
    private String eventId;
    private int seedNumber;
    private SeedStatus status;
    private long startedAt;
    private long confirmedAt;
    private List<Placement> placements = new ArrayList<>();

    public Seed() {}

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

    public int getSeedNumber() {
        return seedNumber;
    }

    public void setSeedNumber(int seedNumber) {
        this.seedNumber = seedNumber;
    }

    public SeedStatus getStatus() {
        return status;
    }

    public void setStatus(SeedStatus status) {
        this.status = status;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(long confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public List<Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(List<Placement> placements) {
        this.placements = placements;
    }
}
