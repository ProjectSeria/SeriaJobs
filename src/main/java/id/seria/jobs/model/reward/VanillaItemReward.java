package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class VanillaItemReward implements Reward {

    private final Material material;
    private final int amount;
    private final String extraMetaKey;

    public VanillaItemReward(Material material, int amount) {
        this(material, amount, null);
    }

    public VanillaItemReward(Material material, int amount, String extraMetaKey) {
        this.material = material;
        this.amount = amount;
        this.extraMetaKey = extraMetaKey;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        if (material == null || material == Material.AIR || amount <= 0) return;

        int finalAmount = amount;
        var hookMgr = SeriaJobs.getInstance().getHookManager();
        if (hookMgr != null) {
            var lp = hookMgr.getLuckPermsHook();
            if (lp != null) {
                int extra = 0;
                if (extraMetaKey != null && !extraMetaKey.isBlank()) {
                    extra = lp.getIntMeta(player, extraMetaKey, 0);
                }
                if (extra == 0) {
                    extra = lp.getExtraJobItems(player, job.getId());
                }
                if (extra > 0) {
                    finalAmount += extra;
                }
            }
        }

        ItemStack item = new ItemStack(material, finalAmount);
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
                // Fallback drop if SeriaCore stash not available
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        String name = material.name().replace('_', ' ').toLowerCase();
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return "<white>+ " + amount + "x " + name + "</white>";
    }
}
