package id.seria.jobs.model;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.reward.Reward;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        this.rewards = rewards;
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
}
