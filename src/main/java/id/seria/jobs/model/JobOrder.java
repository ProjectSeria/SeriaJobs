package id.seria.jobs.model;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.reward.AuraSkillsXPReward;
import id.seria.jobs.model.reward.JobXPReward;
import id.seria.jobs.model.reward.Reward;
import id.seria.jobs.model.reward.VaultReward;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JobOrder {

    private final String id;
    private final String jobId;
    private final String displayName;
    private final List<String> lore;

    // Requirement settings
    private final String source; // "VANILLA", "MMOITEMS", "MYTHICMOBS", "CUSTOMFISHING", "SERIAMAGICBUNDLE", "SERIACRAFTING"
    private final Material material;
    private final Integer customModelData;
    private final boolean strictNbt;
    private final String mmoItemType;
    private final String mmoItemId;
    private final String customItemId; // Generic ID for MythicMobs, CustomFishing, SeriaMagicBundle, SeriaCrafting
    private final int amount;

    // Rewards
    private final List<Reward> rewards;
    private final List<String> customRewardsDisplay;

    // Blueprint Extended Properties
    private final int minReputationAmount;
    private final int maxReputationAmount;
    private final double rewardGins;
    private final int rewardJobXp;
    private final double rewardAuraXp;
    private final double rareDropChance;
    private final List<String> rewardActions;

    public JobOrder(@NotNull String id,
                    @NotNull String jobId,
                    @NotNull String displayName,
                    @NotNull List<String> lore,
                    @NotNull String source,
                    @Nullable Material material,
                    @Nullable Integer customModelData,
                    boolean strictNbt,
                    @Nullable String mmoItemType,
                    @Nullable String mmoItemId,
                    @Nullable String customItemId,
                    int amount,
                    @NotNull List<Reward> rewards) {
        this(id, jobId, displayName, lore, source, material, customModelData, strictNbt,
                mmoItemType, mmoItemId, customItemId, amount, rewards, null);
    }

    public JobOrder(@NotNull String id,
                    @NotNull String jobId,
                    @NotNull String displayName,
                    @NotNull List<String> lore,
                    @NotNull String source,
                    @Nullable Material material,
                    @Nullable Integer customModelData,
                    boolean strictNbt,
                    @Nullable String mmoItemType,
                    @Nullable String mmoItemId,
                    @Nullable String customItemId,
                    int amount,
                    @NotNull List<Reward> rewards,
                    @Nullable List<String> customRewardsDisplay) {
        this(id, jobId, displayName, lore, source, material, customModelData, strictNbt,
                mmoItemType, mmoItemId, customItemId, amount, rewards, customRewardsDisplay,
                0, 0, 0.0, 0, 0.0, 0.0, Collections.emptyList());
    }

    public JobOrder(@NotNull String id,
                    @NotNull String jobId,
                    @NotNull String displayName,
                    @NotNull List<String> lore,
                    @NotNull String source,
                    @Nullable Material material,
                    @Nullable Integer customModelData,
                    boolean strictNbt,
                    @Nullable String mmoItemType,
                    @Nullable String mmoItemId,
                    @Nullable String customItemId,
                    int amount,
                    @NotNull List<Reward> rewards,
                    @Nullable List<String> customRewardsDisplay,
                    int minReputationAmount,
                    int maxReputationAmount,
                    double rewardGins,
                    int rewardJobXp,
                    double rewardAuraXp,
                    double rareDropChance,
                    @Nullable List<String> rewardActions) {
        this.id = id;
        this.jobId = jobId;
        this.displayName = displayName;
        this.lore = lore;
        this.source = source;
        this.material = material;
        this.customModelData = customModelData;
        this.strictNbt = strictNbt;
        this.mmoItemType = mmoItemType;
        this.mmoItemId = mmoItemId;
        this.customItemId = customItemId;
        this.amount = amount;
        this.rewards = rewards != null ? rewards : Collections.emptyList();
        this.customRewardsDisplay = customRewardsDisplay != null ? customRewardsDisplay : Collections.emptyList();

        this.minReputationAmount = minReputationAmount;
        this.maxReputationAmount = maxReputationAmount;
        this.rareDropChance = rareDropChance;
        this.rewardActions = rewardActions != null ? new ArrayList<>(rewardActions) : Collections.emptyList();

        // If explicit rewards are provided (>0), use them; otherwise extract from Reward list
        double computedGins = rewardGins;
        int computedJobXp = rewardJobXp;
        double computedAuraXp = rewardAuraXp;

        if (computedGins <= 0.0) {
            for (Reward r : this.rewards) {
                if (r instanceof VaultReward vr) {
                    computedGins += vr.getAmount();
                }
            }
        }
        if (computedJobXp <= 0) {
            for (Reward r : this.rewards) {
                if (r instanceof JobXPReward jxr) {
                    computedJobXp += jxr.getXp();
                }
            }
        }
        if (computedAuraXp <= 0.0) {
            for (Reward r : this.rewards) {
                if (r instanceof AuraSkillsXPReward axr) {
                    computedAuraXp += axr.getXp();
                }
            }
        }

        this.rewardGins = computedGins;
        this.rewardJobXp = computedJobXp;
        this.rewardAuraXp = computedAuraXp;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getJobId() {
        return jobId;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    @NotNull
    public String getSource() {
        return source;
    }

    @Nullable
    public Material getMaterial() {
        return material;
    }

    @Nullable
    public Integer getCustomModelData() {
        return customModelData;
    }

    public boolean isStrictNbt() {
        return strictNbt;
    }

    @Nullable
    public String getMmoItemType() {
        return mmoItemType;
    }

    @Nullable
    public String getMmoItemId() {
        return mmoItemId;
    }

    @Nullable
    public String getCustomItemId() {
        return customItemId;
    }

    public int getAmount() {
        return amount;
    }

    public int getRequiredAmount(int jobLevel) {
        if (SeriaJobs.getInstance() != null && SeriaJobs.getInstance().getConfigManager() != null) {
            return SeriaJobs.getInstance().getConfigManager().calculateScaledAmount(this.amount, jobLevel);
        }
        return this.amount;
    }

    public int getRequiredAmount(@Nullable Player player, @NotNull Job job) {
        if (player != null && SeriaJobs.getInstance() != null && SeriaJobs.getInstance().getPlayerDataManager() != null) {
            JobProfile profile = SeriaJobs.getInstance().getPlayerDataManager().getProfile(player.getUniqueId());
            if (profile != null) {
                return getRequiredAmount(profile.getJobLevel(job.getId()));
            }
        }
        return this.amount;
    }

    @NotNull
    public List<Reward> getRewards() {
        return Collections.unmodifiableList(rewards);
    }

    @NotNull
    public List<String> getCustomRewardsDisplay() {
        return Collections.unmodifiableList(customRewardsDisplay);
    }

    public boolean hasCustomRewardsDisplay() {
        return customRewardsDisplay != null && !customRewardsDisplay.isEmpty();
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

    /**
     * Converts this JobOrder into an OrderTemplate instance from blueprint.
     */
    @NotNull
    public OrderTemplate toTemplate() {
        return toTemplate(this.amount);
    }

    /**
     * Converts this JobOrder into an OrderTemplate with scaled target amount.
     */
    @NotNull
    public OrderTemplate toTemplate(int targetAmount) {
        JobType jobType = JobType.fromId(this.jobId);
        return new OrderTemplate(
                this.id,
                jobType,
                this.material,
                targetAmount,
                this.minReputationAmount,
                this.maxReputationAmount,
                this.rewardGins,
                this.rewardJobXp,
                this.rewardAuraXp,
                this.rareDropChance,
                this.rewardActions,
                this
        );
    }
}
