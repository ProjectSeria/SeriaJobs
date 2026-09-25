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
        ExcellentEconomyHook hook = SeriaJobs.getInstance().getHookManager().getExcellentEconomyHook();
        if (hook != null && hook.isLoaded()) {
            hook.deposit(player, currencyId, amount);
        } else if (SeriaJobs.getInstance().getHookManager().isVaultLoaded()) {
            SeriaJobs.getInstance().getHookManager().getVaultHook().deposit(player, amount);
        }
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            return customDisplay
                    .replace("{amount}", String.format(java.util.Locale.US, "%,.0f", amount))
                    .replace("{amount_raw}", String.valueOf(amount))
                    .replace("{currency}", currencyId != null ? currencyId : "");
        }
        ExcellentEconomyHook hook = SeriaJobs.getInstance().getHookManager().getExcellentEconomyHook();
        if (hook != null && hook.isLoaded()) {
            String formatted = hook.format(currencyId, amount);
            return "<green>+ " + formatted + "</green>";
        }
        String name = currencyId != null ? currencyId : "Currency";
        return "<green>+ " + String.format(java.util.Locale.US, "%.2f", amount) + " " + name + "</green>";
    }
}
