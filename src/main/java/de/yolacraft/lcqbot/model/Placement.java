package de.yolacraft.lcqbot.model;

public class Placement {
    private String playerId;
    private boolean finished;
    private int place;
    private int points;
    private long finishTimeMs;

    // Gson braucht leeren Konstruktor
    public Placement() {}

    public Placement(String playerId, boolean finished, int place, long finishTimeMs) {
        this.playerId = playerId;
        this.finished = finished;
        this.place = place;
        this.finishTimeMs = finishTimeMs;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public long getFinishTimeMs() {
        return finishTimeMs;
    }

    public void setFinishTimeMs(long finishTimeMs) {
        this.finishTimeMs = finishTimeMs;
    }
}
