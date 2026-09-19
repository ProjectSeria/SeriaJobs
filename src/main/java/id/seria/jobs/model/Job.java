package id.seria.jobs.model;

import id.seria.jobs.model.reward.Reward;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Job {

    private final String id;
    private final String displayName;
    private final Material icon;
    private final List<String> description;

    private final int maxLevel;
    private final int baseXp;
    private final double multiplier;

    private final int activeOrdersCount;
    private final Map<String, JobOrder> ordersPool;
    private final Map<Integer, List<Reward>> levelRewards;

    public Job(@NotNull String id,
               @NotNull String displayName,
               @NotNull Material icon,
               @NotNull List<String> description,
               int maxLevel,
               int baseXp,
               double multiplier,
               int activeOrdersCount,
               @NotNull Map<String, JobOrder> ordersPool,
               @NotNull Map<Integer, List<Reward>> levelRewards) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.maxLevel = maxLevel;
        this.baseXp = baseXp;
        this.multiplier = multiplier;
        this.activeOrdersCount = activeOrdersCount;
        this.ordersPool = ordersPool;
        this.levelRewards = levelRewards;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public Material getIcon() {
        return icon;
    }

    @NotNull
    public List<String> getDescription() {
        return description;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getBaseXp() {
        return baseXp;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getActiveOrdersCount() {
        return activeOrdersCount;
    }

    @NotNull
    public Map<String, JobOrder> getOrdersPool() {
        return Collections.unmodifiableMap(ordersPool);
    }

    @Nullable
    public JobOrder getOrder(String orderId) {
        return ordersPool.get(orderId);
    }

    @NotNull
    public Map<Integer, List<Reward>> getLevelRewards() {
        return Collections.unmodifiableMap(levelRewards);
    }

    @Nullable
    public List<Reward> getRewardsForLevel(int level) {
        return levelRewards.get(level);
    }

    public boolean hasRewardsForLevel(int level) {
        return levelRewards.containsKey(level) && !levelRewards.get(level).isEmpty();
    }

    /**
     * Calculates the required XP to reach the next level.
     * Formula: baseXp * multiplier^(level - 1)
     */
    public int getRequiredXp(int level) {
        if (level >= maxLevel) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.round(baseXp * Math.pow(multiplier, Math.max(0, level - 1)));
    }
}
