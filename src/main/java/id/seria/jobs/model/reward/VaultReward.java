package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VaultReward implements Reward {

    private final double amount;
    private final String customDisplay;

    public VaultReward(double amount) {
        this(amount, null);
    }

    public VaultReward(double amount, String customDisplay) {
        this.amount = amount;
        this.customDisplay = customDisplay;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String getCustomDisplay() {
        return customDisplay;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        if (SeriaJobs.getInstance().getHookManager().isVaultLoaded()) {
            SeriaJobs.getInstance().getHookManager().getVaultHook().deposit(player, amount);
        } else if (SeriaJobs.getInstance().getHookManager().isExcellentEconomyLoaded()) {
            SeriaJobs.getInstance().getHookManager().getExcellentEconomyHook().deposit(player, null, amount);
        }
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            return customDisplay
                    .replace("{amount}", String.format(java.util.Locale.US, "%,.0f", amount))
                    .replace("{amount_raw}", String.valueOf(amount));
        }
        return "<green>+ " + String.format(java.util.Locale.US, "%.2f", amount) + " Saldo Uang</green>";
    }
}
