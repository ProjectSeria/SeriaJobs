package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SeriaCraftingReward implements Reward {

    private final String craftingId;
    private final int amount;

    public SeriaCraftingReward(String craftingId, int amount) {
        this.craftingId = craftingId;
        this.amount = Math.max(1, amount);
    }

    public String getCraftingId() {
        return craftingId;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        if (!SeriaJobs.getInstance().getHookManager().isSeriaCraftingLoaded()) {
            return;
        }

        ItemStack item = SeriaJobs.getInstance().getHookManager().getSeriaCraftingHook().generateItem(craftingId, amount);
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
            // Command fallback for SeriaCrafting
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "seriacrafting give " + player.getName() + " " + craftingId + " " + amount);
        }
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        return "<gold>+ " + amount + "x CraftedItem (" + craftingId + ")</gold>";
    }
}
