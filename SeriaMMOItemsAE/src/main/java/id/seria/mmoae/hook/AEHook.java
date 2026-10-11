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
    private static Method isBlackScrollMethod = null; // isBlackScroll(ItemStack)

    private static final Logger LOGGER = Logger.getLogger("SeriaMMOItemsAE");
    private static final Set<String> WARNED_ENCHANTS = Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

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

                // Check isBlackScroll method
                try {
                    isBlackScrollMethod = aeApiClass.getMethod("isBlackScroll", ItemStack.class);
                } catch (NoSuchMethodException ignored) {}

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
     * Resolves variations of an enchantment name (e.g., ender_slayer -> enderslayer).
     */
    public static List<String> getCandidateNames(String rawName) {
        List<String> list = new ArrayList<>();
        if (rawName == null || rawName.trim().isEmpty()) return list;

        String clean = rawName.trim().toLowerCase();
        list.add(clean);

        String stripped = clean.replace("_", "").replace("-", "");
        if (!list.contains(stripped)) {
            list.add(stripped);
        }

        // Support common spelling fixes
        if (clean.contains("primodial")) {
            String typoFixed = clean.replace("primodial", "primordial");
            if (!list.contains(typoFixed)) list.add(typoFixed);
            String typoFixedStripped = stripped.replace("primodial", "primordial");
            if (!list.contains(typoFixedStripped)) list.add(typoFixedStripped);
        }

        return list;
    }

    /**
     * Applies a single AE enchantment onto an ItemStack, attempting format variations if needed.
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

        List<String> candidates = getCandidateNames(enchantName);
        Throwable lastError = null;

        for (String candidate : candidates) {
            try {
                if (applyEnchantMethod1 != null) {
                    Object result = applyEnchantMethod1.invoke(null, candidate, level, item);
                    if (result instanceof ItemStack) {
                        return (ItemStack) result;
                    }
                } else if (applyEnchantMethod2 != null) {
                    Object result = applyEnchantMethod2.invoke(null, item, candidate, level);
                    if (result instanceof ItemStack) {
                        return (ItemStack) result;
                    }
                }
            } catch (Throwable t) {
                lastError = (t.getCause() != null) ? t.getCause() : t;
            }
        }

        if (lastError != null && WARNED_ENCHANTS.add(enchantName.toLowerCase())) {
            String causeMsg = lastError.getMessage() != null ? lastError.getMessage() : lastError.getClass().getSimpleName();
            LOGGER.log(Level.WARNING, "[SeriaMMOItemsAE] Error applying AE enchant '" + enchantName + " " + level +
                    "' (tried: " + candidates + "): " + causeMsg + ". Please verify enchantment key in AdvancedEnchantments/enchantments.yml.");
        }

        return item;
    }

    /**
     * Checks if the item already has a given enchantment.
     */
    public static boolean hasEnchant(ItemStack item, String enchantName) {
        if (item == null || !available || enchantName == null) return false;

        List<String> candidates = getCandidateNames(enchantName);

        for (String candidate : candidates) {
            try {
                if (hasEnchantMethod1 != null) {
                    Object res = hasEnchantMethod1.invoke(null, candidate, item);
                    if (res instanceof Boolean && (Boolean) res) return true;
                } else if (hasEnchantMethod2 != null) {
                    Object res = hasEnchantMethod2.invoke(null, item, candidate);
                    if (res instanceof Boolean && (Boolean) res) return true;
                }
            } catch (Throwable ignored) {}
        }

        // Fallback: check map
        Map<String, Integer> currentEnchants = getEnchantments(item);
        for (String candidate : candidates) {
            if (currentEnchants.containsKey(candidate)) {
                return true;
            }
        }

        return false;
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
                        String name = entry.getKey().toString().toLowerCase().trim();
                        int lvl = 1;
                        if (entry.getValue() instanceof Number) {
                            lvl = ((Number) entry.getValue()).intValue();
                        } else {
                            try {
                                lvl = Integer.parseInt(entry.getValue().toString().trim());
                            } catch (NumberFormatException ignored) {}
                        }
                        map.put(name, lvl);
                        // Also store stripped version for easy matching
                        String stripped = name.replace("_", "").replace("-", "");
                        if (!map.containsKey(stripped)) {
                            map.put(stripped, lvl);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        return map;
    }

    /**
     * Parses and applies a list of AE enchant specifications.
     */
    public static ItemStack applyEnchantList(ItemStack item, List<String> enchantSpecs) {
        if (item == null || !available || enchantSpecs == null || enchantSpecs.isEmpty()) {
            return item;
        }

        ItemStack currentItem = item;
        Map<String, Integer> existingEnchants = getEnchantments(currentItem);

        for (String spec : enchantSpecs) {
            if (spec == null || spec.trim().isEmpty()) continue;

            String cleaned = spec.replace(":", " ").trim().replaceAll("\\s+", " ");
            String[] tokens = cleaned.split(" ");
            if (tokens.length == 0) continue;

            String enchantName = tokens[0].trim();
            int level = 1;
            if (tokens.length >= 2) {
                try {
                    level = Integer.parseInt(tokens[1].trim());
                } catch (NumberFormatException ignored) {
                    level = 1;
                }
            }

            // Check if already present at same or higher level
            boolean alreadyPresent = false;
            for (String candidate : getCandidateNames(enchantName)) {
                Integer currentLvl = existingEnchants.get(candidate);
                if (currentLvl != null && currentLvl >= level) {
                    alreadyPresent = true;
                    break;
                }
            }
            if (alreadyPresent) {
                continue;
            }

            ItemStack modified = applyEnchant(currentItem, enchantName, level);
            if (modified != null) {
                currentItem = modified;
            }
            for (String candidate : getCandidateNames(enchantName)) {
                existingEnchants.put(candidate, level);
            }
        }

        return currentItem;
    }

    /**
     * Checks if an ItemStack is an AdvancedEnchantments Black Scroll.
     */
    public static boolean isBlackScroll(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        if (isBlackScrollMethod != null) {
            try {
                Object res = isBlackScrollMethod.invoke(null, item);
                if (res instanceof Boolean && (Boolean) res) return true;
            } catch (Throwable ignored) {}
        }
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();
            return name.contains("black scroll");
        }
        return false;
    }
}
