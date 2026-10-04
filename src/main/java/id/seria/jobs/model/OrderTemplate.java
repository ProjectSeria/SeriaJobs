package id.seria.jobs.model;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Representasi data konfigurasi order yang dimuat dari file YAML.
 */
public class OrderTemplate {

    private final String templateId;
    private final JobType jobType;
    private final Material targetMaterial;
    private final int targetAmount;
    private final int minReputationAmount;
    private final int maxReputationAmount;
    private final double rewardGins;
    private final int rewardJobXp;
    private final double rewardAuraXp;
    private final double rareDropChance;
    private final List<String> rewardActions;
    private final JobOrder originalJobOrder;

    public OrderTemplate(@NotNull String templateId,
                         @Nullable JobType jobType,
                         @Nullable Material targetMaterial,
                         int targetAmount,
                         int minReputationAmount,
                         int maxReputationAmount,
                         double rewardGins,
                         int rewardJobXp,
                         double rewardAuraXp,
                         double rareDropChance,
                         @Nullable List<String> rewardActions) {
        this(templateId, jobType, targetMaterial, targetAmount, minReputationAmount, maxReputationAmount,
                rewardGins, rewardJobXp, rewardAuraXp, rareDropChance, rewardActions, null);
    }

    public OrderTemplate(@NotNull String templateId,
                         @Nullable JobType jobType,
                         @Nullable Material targetMaterial,
                         int targetAmount,
                         int minReputationAmount,
                         int maxReputationAmount,
                         double rewardGins,
                         int rewardJobXp,
                         double rewardAuraXp,
                         double rareDropChance,
                         @Nullable List<String> rewardActions,
                         @Nullable JobOrder originalJobOrder) {
        this.templateId = templateId;
        this.jobType = jobType;
        this.targetMaterial = targetMaterial;
        this.targetAmount = targetAmount;
        this.minReputationAmount = minReputationAmount;
        this.maxReputationAmount = maxReputationAmount;
        this.rewardGins = rewardGins;
        this.rewardJobXp = rewardJobXp;
        this.rewardAuraXp = rewardAuraXp;
        this.rareDropChance = rareDropChance;
        this.rewardActions = rewardActions != null ? rewardActions : Collections.emptyList();
        this.originalJobOrder = originalJobOrder;
    }

    @NotNull
    public String getTemplateId() {
        return templateId;
    }

    @Nullable
    public JobType getJobType() {
        return jobType;
    }

    @Nullable
    public Material getTargetMaterial() {
        return targetMaterial;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public int getMinReputationAmount() {
        return minReputationAmount;
    }

    public int getMaxReputationAmount() {
        return maxReputationAmount;
    }

    public double getRewardGins() {
        return rewardGins;
    }

    public int getRewardJobXp() {
        return rewardJobXp;
    }

    public double getRewardAuraXp() {
        return rewardAuraXp;
    }

    public double getRareDropChance() {
        return rareDropChance;
    }

    @NotNull
    public List<String> getRewardActions() {
        return Collections.unmodifiableList(rewardActions);
    }

    @Nullable
    public JobOrder getOriginalJobOrder() {
        return originalJobOrder;
    }

    @Nullable
    public String getEvent() {
        return originalJobOrder != null ? originalJobOrder.getEvent() : null;
    }

    public boolean isEventOrder() {
        return originalJobOrder != null && originalJobOrder.isEventOrder();
    }

    public boolean isRerollable() {
        return originalJobOrder == null || originalJobOrder.isRerollable();
    }
}
