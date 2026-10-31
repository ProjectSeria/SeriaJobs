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
        apply(player, job, 1.0);
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job, double multiplier) {
        double effective = multiplier > 0.0 ? amount * multiplier : amount;
        SeriaJobs.getInstance().getEconomyService().deposit(player, effective);
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        return getFormattedDisplay(1.0);
    }

    @NotNull
    @Override
    public String getFormattedDisplay(double multiplier) {
        double effective = multiplier > 0.0 ? amount * multiplier : amount;
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            return customDisplay
                    .replace("{amount}", String.format(java.util.Locale.US, "%,.0f", effective))
                    .replace("{amount_raw}", String.valueOf(effective));
        }
        return "<green>+ " + String.format(java.util.Locale.US, "%.2f", effective) + " Saldo Uang</green>";
    }
}
