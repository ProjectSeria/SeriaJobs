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
    private final String customDisplay;

    public VanillaItemReward(Material material, int amount) {
        this(material, amount, null, null);
    }

    public VanillaItemReward(Material material, int amount, String extraMetaKey) {
        this(material, amount, extraMetaKey, null);
    }

    public VanillaItemReward(Material material, int amount, String extraMetaKey, String customDisplay) {
        this.material = material;
        this.amount = amount;
        this.extraMetaKey = extraMetaKey;
        this.customDisplay = customDisplay;
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
        if (material == null || material == Material.AIR || amount <= 0) return;

        int finalAmount = multiplier > 0.0 ? Math.max(1, (int) Math.round(amount * multiplier)) : amount;
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
        return getFormattedDisplay(1.0);
    }

    @NotNull
    @Override
    public String getFormattedDisplay(double multiplier) {
        int effective = multiplier > 0.0 ? Math.max(1, (int) Math.round(amount * multiplier)) : amount;
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            String name = material != null ? material.name().replace('_', ' ').toLowerCase() : "Item";
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            return customDisplay
                    .replace("{amount}", String.valueOf(effective))
                    .replace("{item}", name)
                    .replace("{material}", material != null ? material.name() : "");
        }
        String name = material != null ? material.name().replace('_', ' ').toLowerCase() : "Item";
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return "<white>+ " + effective + "x " + name + "</white>";
    }
}
