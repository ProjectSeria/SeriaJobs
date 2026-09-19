package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SeriaMagicBundleReward implements Reward {

    private final String bundleId;
    private final int amount;

    public SeriaMagicBundleReward(String bundleId, int amount) {
        this.bundleId = bundleId;
        this.amount = Math.max(1, amount);
    }

    public String getBundleId() {
        return bundleId;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        if (!SeriaJobs.getInstance().getHookManager().isSeriaMagicBundleLoaded()) {
            return;
        }

        ItemStack item = SeriaJobs.getInstance().getHookManager().getSeriaMagicBundleHook().generateItem(bundleId, amount);
        if (item != null) {
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            if (!leftovers.isEmpty()) {
                for (ItemStack leftover : leftovers.values()) {
                    if (SeriaJobs.getInstance().getHookManager().isSeriaCoreLoaded()) {
                        boolean stashed = SeriaJobs.getInstance().getHookManager().getSeriaCoreHook()
                                .addDelivery(player.getUniqueId(), leftover, "SeriaJobs: " + job.getId());
                        if (stashed) {
                            player.sendMessage(SeriaJobs.getInstance().getLanguageManager().getMessage(player, "orders.stash-delivered"));
                            continue;
                        }
                    }
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
            }
        } else {
            // Command fallback for SeriaMagicBundle
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "magicbundle give " + player.getName() + " " + bundleId + " " + amount);
        }
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        return "<light_purple>+ " + amount + "x MagicBundle (" + bundleId + ")</light_purple>";
    }
}
