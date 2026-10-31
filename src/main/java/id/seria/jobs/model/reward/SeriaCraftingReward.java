package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class SeriaCraftingReward implements Reward {

    private final String craftingId;
    private final int amount;
    private final String customDisplay;

    public SeriaCraftingReward(String craftingId, int amount) {
        this(craftingId, amount, null);
    }

    public SeriaCraftingReward(String craftingId, int amount, String customDisplay) {
        this.craftingId = craftingId;
        this.amount = Math.max(1, amount);
        this.customDisplay = customDisplay;
    }

    public String getCraftingId() {
        return craftingId;
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
        apply(player, job, 1.0);
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job, double multiplier) {
        if (!SeriaJobs.getInstance().getHookManager().isSeriaCraftingLoaded()) {
            return;
        }

        int effective = multiplier > 0.0 ? Math.max(1, (int) Math.round(amount * multiplier)) : amount;
        ItemStack item = SeriaJobs.getInstance().getHookManager().getSeriaCraftingHook().generateItem(craftingId, effective);
        if (item != null) {
            Reward.giveOrStash(player, item, job);
        } else {
            // Command fallback for SeriaCrafting
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "seriacrafting give " + player.getName() + " " + craftingId + " " + effective);
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
                    .replace("{id}", craftingId != null ? craftingId : "");
        }
        return "<gold>+ " + effective + "x CraftedItem (" + craftingId + ")</gold>";
    }
}
