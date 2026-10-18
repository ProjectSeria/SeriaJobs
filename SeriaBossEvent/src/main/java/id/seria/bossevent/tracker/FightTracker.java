package id.seria.bossevent.tracker;

import net.kyori.adventure.text.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FightTracker {

    private final UUID bossUuid;
    private final String mobName;
    private final String eventName;
    private final Map<UUID, Double> damage = new ConcurrentHashMap<>();
    private final Map<UUID, String> names = new ConcurrentHashMap<>();
    private final Map<UUID, Component> prefixes = new ConcurrentHashMap<>();

    private volatile UUID lastDamager;
    private volatile double lastHealth;
    private volatile Instant startTime;
    private volatile boolean dead = false;

    public FightTracker(UUID bossUuid, String mobName, String eventName, double initialHealth) {
        this.bossUuid = bossUuid;
        this.mobName = mobName;
        this.eventName = eventName;
        this.lastHealth = initialHealth;
    }

    public UUID getBossUuid() {
        return bossUuid;
    }

    public String getMobName() {
        return mobName;
    }

    public String getEventName() {
        return eventName;
    }

    public Map<UUID, Double> getDamage() {
        return damage;
    }

    public Map<UUID, String> getNames() {
        return names;
    }

    public Map<UUID, Component> getPrefixes() {
        return prefixes;
    }

    public UUID getLastDamager() {
        return lastDamager;
    }

    public void setLastDamager(UUID lastDamager) {
        this.lastDamager = lastDamager;
    }

    public double getLastHealth() {
        return lastHealth;
    }

    public void setLastHealth(double lastHealth) {
        this.lastHealth = lastHealth;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public void recordAggro(UUID playerUuid, String playerName, Component prefix) {
        this.lastDamager = playerUuid;
        if (playerName != null) {
            this.names.put(playerUuid, playerName);
        }
        if (prefix != null) {
            this.prefixes.put(playerUuid, prefix);
        }
        if (this.startTime == null) {
            this.startTime = Instant.now();
        }
    }

    public void addDamage(UUID playerUuid, double delta) {
        if (playerUuid == null || delta <= 0) return;
        damage.merge(playerUuid, delta, Double::sum);
    }
}
