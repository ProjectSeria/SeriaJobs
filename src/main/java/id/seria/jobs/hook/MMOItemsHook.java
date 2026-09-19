package id.seria.jobs.hook;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MMOItemsHook {

    private boolean available = false;

    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("MMOItems") != null) {
            available = true;
            return true;
        }
        return false;
    }

    public boolean matches(@Nullable ItemStack item, @Nullable String expectedType, @Nullable String expectedId) {
        if (!available || item == null || expectedType == null || expectedId == null) {
            return false;
        }
        try {
            Type itemType = MMOItems.getType(item);
            String itemId = MMOItems.getID(item);
            if (itemType == null || itemId == null) {
                return false;
            }
            return itemType.getId().equalsIgnoreCase(expectedType) && itemId.equalsIgnoreCase(expectedId);
        } catch (Throwable t) {
            return false;
        }
    }

    @Nullable
    public ItemStack getItem(@Nullable String typeName, @Nullable String itemId) {
        return generateItem(typeName, itemId, 1);
    }

    @Nullable
    public ItemStack generateItem(@Nullable String typeName, @Nullable String itemId, int amount) {
        if (!available || typeName == null || itemId == null || amount <= 0) {
            return null;
        }
        try {
            ItemStack item = MMOItems.plugin.getItem(typeName.toUpperCase(), itemId.toUpperCase());
            if (item != null) {
                item.setAmount(amount);
            }
            return item;
        } catch (Throwable t) {
            return null;
        }
    }
}
