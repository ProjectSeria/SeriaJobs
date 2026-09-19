package id.seria.jobs.hook;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class CustomFishingHook {

    private boolean available = false;
    private Object itemManager = null;
    private Method buildMethod = null;
    private Method getItemIdMethod = null;

    public boolean setup() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CustomFishing");
        if (plugin != null && plugin.isEnabled()) {
            try {
                Class<?> pluginClass = Class.forName("net.momirealms.customfishing.api.BukkitCustomFishingPlugin");
                Method getInstance = pluginClass.getMethod("getInstance");
                Object inst = getInstance.invoke(null);

                Method getItemManager = inst.getClass().getMethod("getItemManager");
                this.itemManager = getItemManager.invoke(inst);

                // Look for build or buildInternal method
                for (Method m : itemManager.getClass().getMethods()) {
                    if (m.getName().equals("buildInternal") || m.getName().equals("build") || m.getName().equals("getItem")) {
                        if (m.getParameterCount() == 2 || m.getParameterCount() == 1) {
                            this.buildMethod = m;
                            break;
                        }
                    }
                }

                for (Method m : itemManager.getClass().getMethods()) {
                    if (m.getName().toLowerCase().contains("id") && m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(ItemStack.class)) {
                        this.getItemIdMethod = m;
                        break;
                    }
                }

                available = true;
                return true;
            } catch (Throwable t) {
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
        if (!available || id == null || amount <= 0) return null;

        if (itemManager != null && buildMethod != null) {
            try {
                Object res;
                if (buildMethod.getParameterCount() == 2) {
                    res = buildMethod.invoke(itemManager, null, id);
                } else {
                    res = buildMethod.invoke(itemManager, id);
                }
                if (res instanceof ItemStack is) {
                    ItemStack clone = is.clone();
                    clone.setAmount(amount);
                    return clone;
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public boolean matches(@Nullable ItemStack item, @Nullable String expectedId) {
        if (!available || item == null || expectedId == null) return false;

        if (itemManager != null && getItemIdMethod != null) {
            try {
                Object id = getItemIdMethod.invoke(itemManager, item);
                if (id != null && expectedId.equalsIgnoreCase(id.toString())) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        // PDC & NBT inspection
        try {
            if (item.hasItemMeta()) {
                var pdc = item.getItemMeta().getPersistentDataContainer();
                for (var key : pdc.getKeys()) {
                    if (key.getNamespace().equalsIgnoreCase("customfishing") || key.getKey().toLowerCase().contains("fishing")) {
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
