package id.seria.bossevent.leaderboard;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public class PlayerContribution {

    private final int rank;
    private final UUID uuid;
    private final String name;
    private final Component prefix;
    private final double damage;
    private final double percent;

    public PlayerContribution(int rank, UUID uuid, String name, Component prefix, double damage, double percent) {
        this.rank = rank;
        this.uuid = uuid;
        this.name = name;
        this.prefix = prefix;
        this.damage = damage;
        this.percent = percent;
    }

    public int getRank() {
        return rank;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Component getPrefix() {
        return prefix;
    }

    public double getDamage() {
        return damage;
    }

    public double getPercent() {
        return percent;
    }
}
