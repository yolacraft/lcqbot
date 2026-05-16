package de.yolacraft.lcqbot.seasonleaderboard;

public class SeasonLeaderboardEntry {

    private int[] weeks = new int[4];
    private int total;

    public SeasonLeaderboardEntry() {
        this.weeks = new int[4];
        this.total = 0;
    }

    public int[] getWeeks() {
        if (weeks == null || weeks.length != 4) {
            weeks = new int[4];
        }
        return weeks;
    }

    public void setWeeks(int[] weeks) {
        if (weeks == null || weeks.length != 4) {
            throw new IllegalArgumentException("weeks must contain exactly 4 values");
        }
        this.weeks = weeks;
        recalculateTotal();
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setWeek(int weekIndex, int points) {
        if (weekIndex < 0 || weekIndex > 3) {
            throw new IllegalArgumentException("Week index must be between 0 and 3");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Points must not be negative");
        }
        getWeeks()[weekIndex] = points;
        recalculateTotal();
    }

    public void recalculateTotal() {
        int sum = 0;
        for (int value : getWeeks()) {
            sum += value;
        }
        total = sum;
    }
}
