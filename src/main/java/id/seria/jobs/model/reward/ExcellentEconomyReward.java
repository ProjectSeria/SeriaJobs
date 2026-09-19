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

    public ExcellentEconomyReward(@Nullable String currencyId, double amount) {
        this.currencyId = currencyId;
        this.amount = amount;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public double getAmount() {
        return amount;
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
        ExcellentEconomyHook hook = SeriaJobs.getInstance().getHookManager().getExcellentEconomyHook();
        if (hook != null && hook.isLoaded()) {
            String formatted = hook.format(currencyId, amount);
            return "<green>+ " + formatted + "</green>";
        }
        String name = currencyId != null ? currencyId : "Currency";
        return "<green>+ " + String.format("%.2f", amount) + " " + name + "</green>";
    }
}
