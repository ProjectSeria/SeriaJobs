package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VaultReward implements Reward {

    private final double amount;

    public VaultReward(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
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
        return "<green>+ " + String.format("%.2f", amount) + " Saldo Uang</green>";
    }
}
