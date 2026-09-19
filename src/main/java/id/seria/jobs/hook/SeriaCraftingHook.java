package id.seria.jobs.hook;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class SeriaCraftingHook {

    private boolean available = false;
    private Plugin craftingPlugin = null;

    public boolean setup() {
        craftingPlugin = Bukkit.getPluginManager().getPlugin("SeriaCrafting");
        if (craftingPlugin == null) {
            craftingPlugin = Bukkit.getPluginManager().getPlugin("Crafting");
        }

        if (craftingPlugin != null && craftingPlugin.isEnabled()) {
            available = true;
            return true;
        }
        return false;
    }

    public boolean isAvailable() {
        return available;
    }

    @Nullable
    public ItemStack generateItem(@Nullable String id, int amount) {
        if (!available || id == null || amount <= 0) return null;

        // Try static API or crafting manager reflection
        try {
            Class<?> managerClass = Class.forName("id.seria.crafting.CraftingManager");
            Method getItem = managerClass.getMethod("getItem", String.class);
            Object res = getItem.invoke(null, id);
            if (res instanceof ItemStack is) {
                ItemStack clone = is.clone();
                clone.setAmount(amount);
                return clone;
            }
        } catch (Throwable ignored) {}

        return null;
    }

    public boolean matches(@Nullable ItemStack item, @Nullable String expectedId) {
        if (!available || item == null || expectedId == null) return false;

        try {
            if (item.hasItemMeta()) {
                var pdc = item.getItemMeta().getPersistentDataContainer();
                // Check namespaces seria_crafting, seriacrafting
                for (var key : pdc.getKeys()) {
                    String ns = key.getNamespace().toLowerCase();
                    if (ns.contains("crafting") || ns.contains("seria_crafting")) {
                        String val = pdc.get(key, PersistentDataType.STRING);
                        if (val != null && val.equalsIgnoreCase(expectedId)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }
}
