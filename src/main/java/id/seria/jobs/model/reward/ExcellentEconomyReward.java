package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.hook.ExcellentEconomyHook;
import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExcellentEconomyReward implements Reward {

    private final String currencyId;
    private final double amount;
    private final String customDisplay;

    public ExcellentEconomyReward(@Nullable String currencyId, double amount) {
        this(currencyId, amount, null);
    }

    public ExcellentEconomyReward(@Nullable String currencyId, double amount, @Nullable String customDisplay) {
        this.currencyId = currencyId;
        this.amount = amount;
        this.customDisplay = customDisplay;
    }

    public String getCurrencyId() {
        return currencyId;
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
        ExcellentEconomyHook hook = SeriaJobs.getInstance().getHookManager().getExcellentEconomyHook();
        if (hook != null && hook.isLoaded()) {
            hook.deposit(player, currencyId, effective);
        } else if (SeriaJobs.getInstance().getHookManager().isVaultLoaded()) {
            SeriaJobs.getInstance().getHookManager().getVaultHook().deposit(player, effective);
        }
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
                    .replace("{amount_raw}", String.valueOf(effective))
                    .replace("{currency}", currencyId != null ? currencyId : "");
        }
        ExcellentEconomyHook hook = SeriaJobs.getInstance().getHookManager().getExcellentEconomyHook();
        if (hook != null && hook.isLoaded()) {
            String formatted = hook.format(currencyId, effective);
            return "<green>+ " + formatted + "</green>";
        }
        String name = currencyId != null ? currencyId : "Currency";
        return "<green>+ " + String.format(java.util.Locale.US, "%.2f", effective) + " " + name + "</green>";
    }
}
