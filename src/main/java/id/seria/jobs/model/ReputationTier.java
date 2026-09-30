package id.seria.jobs.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Model representasi satu tingkatan (tier) reputasi pada sistem list konfigurasi.
 */
public class ReputationTier {

    private final int requiredReputation;
    private final double rewardMultiplier;
    private final double orderMultiplier;
    private final int slots;
    private final String displayName;

    public ReputationTier(int requiredReputation,
                          double rewardMultiplier,
                          double orderMultiplier,
                          int slots,
                          @Nullable String displayName) {
        this.requiredReputation = requiredReputation;
        this.rewardMultiplier = rewardMultiplier > 0.0 ? rewardMultiplier : 1.0;
        this.orderMultiplier = orderMultiplier > 0.0 ? orderMultiplier : 1.0;
        this.slots = slots;
        this.displayName = (displayName != null && !displayName.isBlank())
                ? displayName
                : "Tier " + requiredReputation;
    }

    public int getRequiredReputation() {
        return requiredReputation;
    }

    public double getRewardMultiplier() {
        return rewardMultiplier;
    }

    public double getOrderMultiplier() {
        return orderMultiplier;
    }

    public int getSlots() {
        return slots;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }
}
