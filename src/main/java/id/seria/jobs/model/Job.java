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
    private final String auraSkillName;
    private final JobType jobType;

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
                levelDisplayRewards, Collections.emptyMap(), null);
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
        this(id, displayName, icon, description, maxLevel, baseXp, multiplier,
                levelingProgressionEquation, incomeProgressionEquation, pointsProgressionEquation,
                experienceProgressionEquation, activeOrdersCount, ordersPool, levelRewards,
                levelDisplayRewards, levelIcons, null);
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
               @NotNull Map<Integer, org.bukkit.configuration.ConfigurationSection> levelIcons,
               @Nullable String auraSkillName) {
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
        this.auraSkillName = (auraSkillName != null && !auraSkillName.isBlank()) ? auraSkillName : id.toLowerCase();
        this.jobType = JobType.getOrCreate(this.id, this.displayName, this.auraSkillName);
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
    public String getAuraSkillName() {
        return auraSkillName;
    }

    @NotNull
    public JobType getJobType() {
        return jobType;
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
        if (orderId == null) return null;
        JobOrder jo = ordersPool.get(orderId);
        if (jo != null) return jo;
        jo = ordersPool.get(orderId.toLowerCase());
        if (jo != null) return jo;
        for (JobOrder o : ordersPool.values()) {
            if (o.getId().equalsIgnoreCase(orderId)) {
                return o;
            }
        }
        return null;
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
     * XP yang dibutuhkan untuk naik dari {@code level} ke level berikutnya.
     *
     * <p>Urutan sumber:
     * <ol>
     *   <li>{@code leveling-progression-equation} di file job ({@code joblevel} diganti level saat ini),
     *       dievaluasi lewat {@code $e()} SeriaCore</li>
     *   <li>Fallback: {@code leveling.base-xp × leveling.multiplier^(level-1)}</li>
     * </ol>
     * Hasil dibatasi ke {@link Integer#MAX_VALUE} supaya rumus eksponensial di level tinggi
     * tidak overflow jadi angka negatif.
     *
     * @return XP yang dibutuhkan, atau {@link Integer#MAX_VALUE} bila sudah max level
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
                    return clampToInt(val);
                }
            } catch (Exception ignored) {
                // Rumus tidak valid -> pakai fallback di bawah
            }
        }

        return clampToInt(baseXp * Math.pow(multiplier, Math.max(0, level - 1)));
    }

    /** Bulatkan lalu batasi ke rentang int positif (minimal 1 XP). */
    private static int clampToInt(double value) {
        if (value >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) Math.max(1, Math.round(value));
    }
}
