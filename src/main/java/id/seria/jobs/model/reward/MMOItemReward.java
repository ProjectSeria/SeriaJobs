package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MMOItemReward implements Reward {

    private final String mmoType;
    private final String mmoId;
    private final int amount;

    public MMOItemReward(String mmoType, String mmoId, int amount) {
        this.mmoType = mmoType;
        this.mmoId = mmoId;
        this.amount = amount;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        if (!SeriaJobs.getInstance().getHookManager().isMmoItemsLoaded()) {
            return;
        }

        ItemStack item = SeriaJobs.getInstance().getHookManager().getMmoItemsHook().generateItem(mmoType, mmoId, amount);
        if (item == null) {
            SeriaJobs.getInstance().getLogger().warning("Failed to generate MMOItem reward: " + mmoType + ":" + mmoId);
            return;
        }

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
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        return "<light_purple>+ " + amount + "x MMOItem (" + mmoId + ")</light_purple>";
    }
}
