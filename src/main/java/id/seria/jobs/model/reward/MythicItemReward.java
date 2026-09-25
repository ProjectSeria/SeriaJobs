package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MythicItemReward implements Reward {

    private final String itemId;
    private final int amount;
    private final String customDisplay;

    public MythicItemReward(String itemId, int amount) {
        this(itemId, amount, null);
    }

    public MythicItemReward(String itemId, int amount, String customDisplay) {
        this.itemId = itemId;
        this.amount = Math.max(1, amount);
        this.customDisplay = customDisplay;
    }

    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String getCustomDisplay() {
        return customDisplay;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        if (!SeriaJobs.getInstance().getHookManager().isMythicMobsLoaded()) {
            return;
        }

        ItemStack item = SeriaJobs.getInstance().getHookManager().getMythicMobsHook().generateItem(itemId, amount);
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
            // Command fallback for MythicMobs
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm i give " + player.getName() + " " + itemId + " " + amount);
        }
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            return customDisplay
                    .replace("{amount}", String.valueOf(amount))
                    .replace("{id}", itemId != null ? itemId : "");
        }
        return "<light_purple>+ " + amount + "x MythicItem (" + itemId + ")</light_purple>";
    }
}
