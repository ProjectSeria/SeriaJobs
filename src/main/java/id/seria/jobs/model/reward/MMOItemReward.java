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
    private final String customDisplay;

    public MMOItemReward(String mmoType, String mmoId, int amount) {
        this(mmoType, mmoId, amount, null);
    }

    public MMOItemReward(String mmoType, String mmoId, int amount, String customDisplay) {
        this.mmoType = mmoType;
        this.mmoId = mmoId;
        this.amount = amount;
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
        if (!SeriaJobs.getInstance().getHookManager().isMmoItemsLoaded()) {
            return;
        }

        int effective = multiplier > 0.0 ? Math.max(1, (int) Math.round(amount * multiplier)) : amount;
        ItemStack item = SeriaJobs.getInstance().getHookManager().getMmoItemsHook().generateItem(mmoType, mmoId, effective);
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
        return getFormattedDisplay(1.0);
    }

    @NotNull
    @Override
    public String getFormattedDisplay(double multiplier) {
        int effective = multiplier > 0.0 ? Math.max(1, (int) Math.round(amount * multiplier)) : amount;
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            return customDisplay
                    .replace("{amount}", String.valueOf(effective))
                    .replace("{id}", mmoId != null ? mmoId : "")
                    .replace("{type}", mmoType != null ? mmoType : "");
        }
        return "<light_purple>+ " + effective + "x MMOItem (" + mmoId + ")</light_purple>";
    }
}
