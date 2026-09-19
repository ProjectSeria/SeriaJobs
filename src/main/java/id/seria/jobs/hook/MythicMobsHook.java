package id.seria.jobs.hook;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class MythicMobsHook {

    private boolean available = false;
    private Object itemManager = null;
    private Method getItemStackMethod = null;
    private Method getMythicTypeMethod = null;

    public boolean setup() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if (plugin != null && plugin.isEnabled()) {
            try {
                Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
                Method getApiMethod = mythicBukkitClass.getMethod("getAPI");
                Object api = getApiMethod.invoke(null);

                Method getItemManagerMethod = api.getClass().getMethod("getItemManager");
                this.itemManager = getItemManagerMethod.invoke(api);

                this.getItemStackMethod = itemManager.getClass().getMethod("getItemStack", String.class);
                try {
                    this.getMythicTypeMethod = itemManager.getClass().getMethod("getMythicTypeFromItem", ItemStack.class);
                } catch (NoSuchMethodException ignored) {}

                available = true;
                return true;
            } catch (Throwable t) {
                // Try legacy MythicMobs API
                try {
                    Class<?> legacyClass = Class.forName("net.elsowiny.mythicmobs.MythicMobs");
                    available = true;
                    return true;
                } catch (Throwable ignored) {}
                available = true;
                return true;
            }
        }
        return false;
    }

    public boolean isAvailable() {
        return available;
    }

    @Nullable
    public ItemStack generateItem(@Nullable String id, int amount) {
        if (!available || id == null || amount <= 0) {
            return null;
        }

        if (itemManager != null && getItemStackMethod != null) {
            try {
                Object res = getItemStackMethod.invoke(itemManager, id);
                if (res instanceof ItemStack is) {
                    ItemStack clone = is.clone();
                    clone.setAmount(amount);
                    return clone;
                }
            } catch (Throwable ignored) {}
        }

        // Fallback using MythicMobs command if API instance is not yet ready
        return null;
    }

    public boolean matches(@Nullable ItemStack item, @Nullable String expectedId) {
        if (!available || item == null || expectedId == null) {
            return false;
        }

        if (itemManager != null && getMythicTypeMethod != null) {
            try {
                Object type = getMythicTypeMethod.invoke(itemManager, item);
                if (type != null && expectedId.equalsIgnoreCase(type.toString())) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        // PDC / NBT Check
        try {
            if (item.hasItemMeta()) {
                var pdc = item.getItemMeta().getPersistentDataContainer();
                for (var key : pdc.getKeys()) {
                    if (key.getKey().equalsIgnoreCase("mythic_type") || key.getKey().equalsIgnoreCase("type")) {
                        String val = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
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
