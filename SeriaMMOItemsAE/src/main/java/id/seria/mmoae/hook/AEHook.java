package id.seria.mmoae.hook;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AEHook {

    private static boolean available = false;
    private static Class<?> aeApiClass = null;
    private static Method applyEnchantMethod1 = null; // applyEnchant(String, int, ItemStack)
    private static Method applyEnchantMethod2 = null; // applyEnchant(ItemStack, String, int)
    private static Method hasEnchantMethod1 = null;   // hasEnchant(String, ItemStack)
    private static Method hasEnchantMethod2 = null;   // hasEnchant(ItemStack, String)
    private static Method getEnchantsMethod = null;   // getEnchantments(ItemStack) or getEnchantsOnItem(ItemStack)

    private static final Logger LOGGER = Logger.getLogger("SeriaMMOItemsAE");

    public static void initialize() {
        Plugin aePlugin = Bukkit.getPluginManager().getPlugin("AdvancedEnchantments");
        if (aePlugin != null && aePlugin.isEnabled()) {
            try {
                aeApiClass = Class.forName("net.advancedplugins.ae.api.AEAPI");

                // Check applyEnchant methods
                try {
                    applyEnchantMethod1 = aeApiClass.getMethod("applyEnchant", String.class, int.class, ItemStack.class);
                } catch (NoSuchMethodException ignored) {}

                try {
                    applyEnchantMethod2 = aeApiClass.getMethod("applyEnchant", ItemStack.class, String.class, int.class);
                } catch (NoSuchMethodException ignored) {}

                // Check hasEnchant methods
                try {
                    hasEnchantMethod1 = aeApiClass.getMethod("hasEnchant", String.class, ItemStack.class);
                } catch (NoSuchMethodException ignored) {}

                try {
                    hasEnchantMethod2 = aeApiClass.getMethod("hasEnchant", ItemStack.class, String.class);
                } catch (NoSuchMethodException ignored) {}

                // Check getEnchantments method
                try {
                    getEnchantsMethod = aeApiClass.getMethod("getEnchantments", ItemStack.class);
                } catch (NoSuchMethodException e) {
                    try {
                        getEnchantsMethod = aeApiClass.getMethod("getEnchantsOnItem", ItemStack.class);
                    } catch (NoSuchMethodException ignored) {}
                }

                available = (applyEnchantMethod1 != null || applyEnchantMethod2 != null);
                if (available) {
                    LOGGER.info("[SeriaMMOItemsAE] Hooked into AdvancedEnchantments successfully!");
                } else {
                    LOGGER.warning("[SeriaMMOItemsAE] Found AdvancedEnchantments, but AEAPI.applyEnchant method was not found.");
                }
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[SeriaMMOItemsAE] Failed to initialize AdvancedEnchantments hook: " + t.getMessage(), t);
                available = false;
            }
        } else {
            available = false;
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    /**
     * Applies a single AE enchantment onto an ItemStack.
     *
     * @param item The source item stack.
     * @param enchantName The name/key of the enchantment (e.g., "decapitation").
     * @param level The level of the enchantment.
     * @return The modified ItemStack containing AE NBT, hashes, and lore.
     */
    public static ItemStack applyEnchant(ItemStack item, String enchantName, int level) {
        if (item == null || !available || enchantName == null || enchantName.trim().isEmpty() || level <= 0) {
            return item;
        }

        try {
            if (applyEnchantMethod1 != null) {
                Object result = applyEnchantMethod1.invoke(null, enchantName.trim(), level, item);
                if (result instanceof ItemStack) {
                    return (ItemStack) result;
                }
            } else if (applyEnchantMethod2 != null) {
                Object result = applyEnchantMethod2.invoke(null, item, enchantName.trim(), level);
                if (result instanceof ItemStack) {
                    return (ItemStack) result;
                }
            }
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "[SeriaMMOItemsAE] Error applying AE enchant '" + enchantName + " " + level + "': " + t.getMessage());
        }

        return item;
    }

    /**
     * Checks if the item already has a given enchantment.
     */
    public static boolean hasEnchant(ItemStack item, String enchantName) {
        if (item == null || !available || enchantName == null) return false;

        try {
            if (hasEnchantMethod1 != null) {
                Object res = hasEnchantMethod1.invoke(null, enchantName.trim(), item);
                if (res instanceof Boolean) return (Boolean) res;
            } else if (hasEnchantMethod2 != null) {
                Object res = hasEnchantMethod2.invoke(null, item, enchantName.trim());
                if (res instanceof Boolean) return (Boolean) res;
            }
        } catch (Throwable ignored) {}

        // Fallback: check map
        Map<String, Integer> currentEnchants = getEnchantments(item);
        return currentEnchants.containsKey(enchantName.trim().toLowerCase());
    }

    /**
     * Retrieves all AE enchantments on the item.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Integer> getEnchantments(ItemStack item) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (item == null || !available || getEnchantsMethod == null) return map;

        try {
            Object result = getEnchantsMethod.invoke(null, item);
            if (result instanceof Map) {
                Map<?, ?> rawMap = (Map<?, ?>) result;
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        String name = entry.getKey().toString().toLowerCase();
                        int lvl = 1;
                        if (entry.getValue() instanceof Number) {
                            lvl = ((Number) entry.getValue()).intValue();
                        } else {
                            try {
                                lvl = Integer.parseInt(entry.getValue().toString());
                            } catch (NumberFormatException ignored) {}
                        }
                        map.put(name, lvl);
                    }
                }
            }
        } catch (Throwable ignored) {}

        return map;
    }

    /**
     * Parses and applies a list of AE enchant specifications (e.g., ["decapitation 3", "critical 3"]).
     */
    public static ItemStack applyEnchantList(ItemStack item, List<String> enchantSpecs) {
        if (item == null || !available || enchantSpecs == null || enchantSpecs.isEmpty()) {
            return item;
        }

        ItemStack currentItem = item;
        Map<String, Integer> existingEnchants = getEnchantments(currentItem);

        for (String spec : enchantSpecs) {
            if (spec == null || spec.trim().isEmpty()) continue;

            String cleaned = spec.trim();
            String[] tokens = cleaned.split("\\s+");
            if (tokens.length == 0) continue;

            String enchantName = tokens[0];
            int level = 1;
            if (tokens.length >= 2) {
                try {
                    level = Integer.parseInt(tokens[1]);
                } catch (NumberFormatException ignored) {
                    level = 1;
                }
            }

            // Check if already present at same or higher level
            Integer currentLvl = existingEnchants.get(enchantName.toLowerCase());
            if (currentLvl != null && currentLvl >= level) {
                continue;
            }

            ItemStack modified = applyEnchant(currentItem, enchantName, level);
            if (modified != null) {
                currentItem = modified;
                existingEnchants.put(enchantName.toLowerCase(), level);
            }
        }

        return currentItem;
    }
}
