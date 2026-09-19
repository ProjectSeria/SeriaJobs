package id.seria.jobs.model;

import java.util.UUID;

public class LeaderboardEntry {

    private final UUID uuid;
    private final String playerName;
    private final int rank;
    private final int score;
    private final int level;
    private final int xp;
    private final int completedOrders;

    public LeaderboardEntry(UUID uuid, String playerName, int rank, int score, int level, int xp, int completedOrders) {
        this.uuid = uuid;
        this.playerName = playerName != null && !playerName.isBlank() ? playerName : "Unknown";
        this.rank = rank;
        this.score = score;
        this.level = level;
        this.xp = xp;
        this.completedOrders = completedOrders;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getRank() {
        return rank;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public int getOrdersCompleted() {
        return completedOrders;
    }
}
