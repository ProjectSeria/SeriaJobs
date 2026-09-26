package id.seria.jobs.model;

import id.seria.jobs.utils.ItemMatcher;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Instance kontrak kerja aktif yang berada di dalam slot pemain.
 */
public class WorkOrder {

    private final UUID orderId;
    private final OrderTemplate template;
    private final ItemStack targetItem;
    private final int generatedReputationAmount;
    private final double rewardGins;
    private final int rewardJobXp;
    private final double rewardAuraXp;
    private final double rareDropChance;
    private final List<String> actions;
    private boolean completed;

    public WorkOrder(@NotNull UUID orderId,
                     @NotNull OrderTemplate template,
                     @Nullable ItemStack targetItem,
                     int generatedReputationAmount,
                     double rewardGins,
                     int rewardJobXp,
                     double rewardAuraXp,
                     double rareDropChance,
                     @Nullable List<String> actions,
                     boolean completed) {
        this.orderId = orderId;
        this.template = template;
        this.targetItem = targetItem != null ? targetItem.clone() : (template.getTargetMaterial() != null ? new ItemStack(template.getTargetMaterial(), template.getTargetAmount()) : new ItemStack(Material.STONE, template.getTargetAmount()));
        this.generatedReputationAmount = generatedReputationAmount;
        this.rewardGins = rewardGins;
        this.rewardJobXp = rewardJobXp;
        this.rewardAuraXp = rewardAuraXp;
        this.rareDropChance = rareDropChance;
        this.actions = actions != null ? new ArrayList<>(actions) : new ArrayList<>(template.getRewardActions());
        this.completed = completed;
    }

    @NotNull
    public UUID getOrderId() {
        return orderId;
    }

    @NotNull
    public OrderTemplate getTemplate() {
        return template;
    }

    @NotNull
    public ItemStack getTargetItem() {
        return targetItem.clone();
    }

    public int getGeneratedReputationAmount() {
        return generatedReputationAmount;
    }

    public double getRewardGins() {
        return rewardGins;
    }

    public int getRewardJobXp() {
        return rewardJobXp;
    }

    public double getRewardAuraXp() {
        return rewardAuraXp;
    }

    public double getRareDropChance() {
        return rareDropChance;
    }

    @NotNull
    public List<String> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Memeriksa apakah inventaris pemain memiliki material target dalam jumlah yang cukup.
     */
    public boolean canFulfill(@NotNull Player player) {
        if (completed) return false;
        JobOrder orig = template.getOriginalJobOrder();
        if (orig != null) {
            int count = ItemMatcher.countMatchingItems(player, orig);
            return count >= template.getTargetAmount();
        }

        // Vanilla check by target material
        Material mat = targetItem.getType();
        int needed = template.getTargetAmount();
        int found = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) {
                found += item.getAmount();
                if (found >= needed) return true;
            }
        }
        return found >= needed;
    }

    /**
     * Menarik item target dari inventaris pemain.
     */
    public void consumeItems(@NotNull Player player) {
        JobOrder orig = template.getOriginalJobOrder();
        int needed = template.getTargetAmount();
        if (orig != null) {
            ItemMatcher.removeMatchingItems(player, orig, needed);
            return;
        }

        Material mat = targetItem.getType();
        int remaining = needed;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == mat) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                    break;
                }
                if (remaining <= 0) break;
            }
        }
    }
}
