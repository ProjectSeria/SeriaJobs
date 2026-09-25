package id.seria.jobs.model;

import id.seria.core.gui.menu.MenuRequirementEvaluator;
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

    private final String levelingProgressionEquation;
    private final String incomeProgressionEquation;
    private final String pointsProgressionEquation;
    private final String experienceProgressionEquation;

    private final int activeOrdersCount;
    private final Map<String, JobOrder> ordersPool;
    private final Map<Integer, List<Reward>> levelRewards;
    private final Map<Integer, List<String>> levelDisplayRewards;
    private final Map<Integer, org.bukkit.configuration.ConfigurationSection> levelIcons;

    public Job(@NotNull String id,
               @NotNull String displayName,
               @NotNull Material icon,
               @NotNull List<String> description,
               int maxLevel,
               int baseXp,
               double multiplier,
               @Nullable String levelingProgressionEquation,
               @Nullable String incomeProgressionEquation,
               @Nullable String pointsProgressionEquation,
               @Nullable String experienceProgressionEquation,
               int activeOrdersCount,
               @NotNull Map<String, JobOrder> ordersPool,
               @NotNull Map<Integer, List<Reward>> levelRewards,
               @NotNull Map<Integer, List<String>> levelDisplayRewards) {
        this(id, displayName, icon, description, maxLevel, baseXp, multiplier,
                levelingProgressionEquation, incomeProgressionEquation, pointsProgressionEquation,
                experienceProgressionEquation, activeOrdersCount, ordersPool, levelRewards,
                levelDisplayRewards, Collections.emptyMap());
    }

    public Job(@NotNull String id,
               @NotNull String displayName,
               @NotNull Material icon,
               @NotNull List<String> description,
               int maxLevel,
               int baseXp,
               double multiplier,
               @Nullable String levelingProgressionEquation,
               @Nullable String incomeProgressionEquation,
               @Nullable String pointsProgressionEquation,
               @Nullable String experienceProgressionEquation,
               int activeOrdersCount,
               @NotNull Map<String, JobOrder> ordersPool,
               @NotNull Map<Integer, List<Reward>> levelRewards,
               @NotNull Map<Integer, List<String>> levelDisplayRewards,
               @NotNull Map<Integer, org.bukkit.configuration.ConfigurationSection> levelIcons) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.maxLevel = maxLevel;
        this.baseXp = baseXp;
        this.multiplier = multiplier;
        this.levelingProgressionEquation = levelingProgressionEquation;
        this.incomeProgressionEquation = incomeProgressionEquation;
        this.pointsProgressionEquation = pointsProgressionEquation;
        this.experienceProgressionEquation = experienceProgressionEquation;
        this.activeOrdersCount = activeOrdersCount;
        this.ordersPool = ordersPool;
        this.levelRewards = levelRewards;
        this.levelDisplayRewards = levelDisplayRewards;
        this.levelIcons = levelIcons != null ? levelIcons : Collections.emptyMap();
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

    @Nullable
    public String getLevelingProgressionEquation() {
        return levelingProgressionEquation;
    }

    @Nullable
    public String getIncomeProgressionEquation() {
        return incomeProgressionEquation;
    }

    @Nullable
    public String getPointsProgressionEquation() {
        return pointsProgressionEquation;
    }

    @Nullable
    public String getExperienceProgressionEquation() {
        return experienceProgressionEquation;
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

    @NotNull
    public Map<Integer, List<String>> getLevelDisplayRewards() {
        return Collections.unmodifiableMap(levelDisplayRewards);
    }

    @Nullable
    public List<Reward> getRewardsForLevel(int level) {
        return levelRewards.get(level);
    }

    @Nullable
    public List<String> getDisplayRewardsForLevel(int level) {
        return levelDisplayRewards.get(level);
    }

    public boolean hasRewardsForLevel(int level) {
        return (levelRewards.containsKey(level) && !levelRewards.get(level).isEmpty()) || levelIcons.containsKey(level);
    }

    @NotNull
    public Map<Integer, org.bukkit.configuration.ConfigurationSection> getLevelIcons() {
        return Collections.unmodifiableMap(levelIcons);
    }

    @Nullable
    public org.bukkit.configuration.ConfigurationSection getLevelIcon(int level) {
        return levelIcons.get(level);
    }

    /**
     * Calculates the required XP to reach the next level.
     * Evaluates polynomial levelingProgressionEquation if present, otherwise defaults to exponential.
     */
    public int getRequiredXp(int level) {
        if (level >= maxLevel) {
            return Integer.MAX_VALUE;
        }

        if (levelingProgressionEquation != null && !levelingProgressionEquation.isBlank()) {
            try {
                String expr = levelingProgressionEquation.replace("joblevel", String.valueOf(level));
                String eval = MenuRequirementEvaluator.evaluateMathExpressions("$e(" + expr + ")");
                double val = Double.parseDouble(eval.trim());
                if (val > 0) {
                    return (int) Math.round(val);
                }
            } catch (Exception ignored) {}
        }

        return (int) Math.round(baseXp * Math.pow(multiplier, Math.max(0, level - 1)));
    }
}
