package id.seria.jobs.hook;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.utils.ItemMatcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class SeriaMagicBundleHook {

    private boolean available = false;
    private Plugin bundlePlugin = null;
    private Object storageManager = null;
    private Field cacheField = null;
    private Field itemCacheField = null;
    private Method loadPlayerDataMethod = null;
    private Method addAmountMethod = null;
    private Method removeItemStackMethod = null;

    public synchronized boolean setup() {
        bundlePlugin = Bukkit.getPluginManager().getPlugin("SeriaMagicBundle");
        if (bundlePlugin == null) {
            bundlePlugin = Bukkit.getPluginManager().getPlugin("MagicBundle");
        }

        if (bundlePlugin != null && bundlePlugin.isEnabled()) {
            initReflection();
            available = (storageManager != null);
            return available;
        }
        available = false;
        return false;
    }

    private void initReflection() {
        if (bundlePlugin == null) return;
        try {
            Method getStorageManagerMethod = bundlePlugin.getClass().getMethod("getStorageManager");
            storageManager = getStorageManagerMethod.invoke(bundlePlugin);
            if (storageManager != null) {
                cacheField = storageManager.getClass().getDeclaredField("cache");
                cacheField.setAccessible(true);

                itemCacheField = storageManager.getClass().getDeclaredField("itemCache");
                itemCacheField.setAccessible(true);

                try {
                    loadPlayerDataMethod = storageManager.getClass().getMethod("loadPlayerData", UUID.class);
                } catch (Throwable ignored) {}

                addAmountMethod = storageManager.getClass().getMethod("addAmount", Player.class, String.class, String.class, int.class);
                removeItemStackMethod = storageManager.getClass().getMethod("removeItemStack", Player.class, String.class, int.class);
            }
        } catch (Throwable t) {
            SeriaJobs.getInstance().getLogger().warning("Failed to initialize SeriaMagicBundle reflection: " + t.getMessage());
        }
    }

    public boolean isAvailable() {
        if (!available || bundlePlugin == null || !bundlePlugin.isEnabled() || storageManager == null) {
            setup();
        }
        return available && bundlePlugin != null && bundlePlugin.isEnabled() && storageManager != null;
    }

    public Plugin getBundlePlugin() {
        return bundlePlugin;
    }

    // ==========================================
    // VIRTUAL STORAGE INTEGRATION (SeriaMagicBundle)
    // ==========================================

    @SuppressWarnings("unchecked")
    public int countVirtualStorageItems(@NotNull Player player, @NotNull JobOrder order) {
        if (!isAvailable() || storageManager == null) return 0;
        int total = 0;
        UUID uuid = player.getUniqueId();

        try {
            // Ensure player data is loaded in StorageManager cache
            if (loadPlayerDataMethod != null) {
                try {
                    loadPlayerDataMethod.invoke(storageManager, uuid);
                } catch (Throwable ignored) {}
            }

            // 1. Grouped items in cache (Map<UUID, Map<String, Map<String, Integer>>>)
            if (cacheField != null) {
                Map<UUID, Map<String, Map<String, Integer>>> cache =
                        (Map<UUID, Map<String, Map<String, Integer>>>) cacheField.get(storageManager);
                if (cache != null) {
                    Map<String, Map<String, Integer>> playerMap = cache.get(uuid);
                    if (playerMap != null) {
                        for (Map.Entry<String, Map<String, Integer>> collEntry : playerMap.entrySet()) {
                            for (Map.Entry<String, Integer> itemEntry : collEntry.getValue().entrySet()) {
                                if (itemKeyMatchesOrder(itemEntry.getKey(), order)) {
                                    int val = Math.max(0, itemEntry.getValue());
                                    total += val;
                                }
                            }
                        }
                    }
                }
            }

            // 2. Individual items in itemCache (e.g. fishing, custom items)
            if (itemCacheField != null) {
                Map<UUID, Map<String, List<?>>> itemCache =
                        (Map<UUID, Map<String, List<?>>>) itemCacheField.get(storageManager);
                if (itemCache != null) {
                    Map<String, List<?>> playerCategories = itemCache.get(uuid);
                    if (playerCategories != null) {
                        for (List<?> list : playerCategories.values()) {
                            if (list == null) continue;
                            for (Object storedObj : list) {
                                if (storedObj == null) continue;
                                Field itemField = storedObj.getClass().getField("item");
                                ItemStack is = (ItemStack) itemField.get(storedObj);
                                if (is != null && ItemMatcher.matches(is, order)) {
                                    total += is.getAmount();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            SeriaJobs.getInstance().getLogger().warning("Error reading SeriaMagicBundle virtual storage: " + t.getMessage());
        }

        return total;
    }

    @SuppressWarnings("unchecked")
    public int withdrawVirtualStorageItems(@NotNull Player player, @NotNull JobOrder order, int amountNeeded) {
        if (!isAvailable() || storageManager == null || amountNeeded <= 0) return 0;
        int remaining = amountNeeded;
        UUID uuid = player.getUniqueId();

        try {
            if (loadPlayerDataMethod != null) {
                try {
                    loadPlayerDataMethod.invoke(storageManager, uuid);
                } catch (Throwable ignored) {}
            }

            // 1. Grouped items in cache
            if (cacheField != null && addAmountMethod != null) {
                Map<UUID, Map<String, Map<String, Integer>>> cache =
                        (Map<UUID, Map<String, Map<String, Integer>>>) cacheField.get(storageManager);
                if (cache != null) {
                    Map<String, Map<String, Integer>> playerMap = cache.get(uuid);
                    if (playerMap != null) {
                        for (Map.Entry<String, Map<String, Integer>> collEntry : playerMap.entrySet()) {
                            String collId = collEntry.getKey();
                            for (Map.Entry<String, Integer> itemEntry : collEntry.getValue().entrySet()) {
                                String itemKey = itemEntry.getKey();
                                int currentStored = Math.max(0, itemEntry.getValue());
                                if (currentStored > 0 && itemKeyMatchesOrder(itemKey, order)) {
                                    int toDeduct = Math.min(currentStored, remaining);
                                    addAmountMethod.invoke(storageManager, player, collId, itemKey, -toDeduct);
                                    remaining -= toDeduct;
                                    if (remaining <= 0) break;
                                }
                            }
                            if (remaining <= 0) break;
                        }
                    }
                }
            }

            // 2. Individual items in itemCache (e.g. fishing, custom items)
            if (remaining > 0 && itemCacheField != null && removeItemStackMethod != null) {
                Map<UUID, Map<String, List<?>>> itemCache =
                        (Map<UUID, Map<String, List<?>>>) itemCacheField.get(storageManager);
                if (itemCache != null) {
                    Map<String, List<?>> playerCategories = itemCache.get(uuid);
                    if (playerCategories != null) {
                        for (Map.Entry<String, List<?>> catEntry : playerCategories.entrySet()) {
                            String catId = catEntry.getKey();
                            List<?> list = catEntry.getValue();
                            if (list == null) continue;
                            List<?> copyList = new ArrayList<>(list);
                            for (Object storedObj : copyList) {
                                if (storedObj == null) continue;
                                Field idField = storedObj.getClass().getField("id");
                                Field itemField = storedObj.getClass().getField("item");
                                int dbId = idField.getInt(storedObj);
                                ItemStack is = (ItemStack) itemField.get(storedObj);
                                if (is != null && ItemMatcher.matches(is, order)) {
                                    int stackAmt = is.getAmount();
                                    if (stackAmt <= remaining) {
                                        removeItemStackMethod.invoke(storageManager, player, catId, dbId);
                                        remaining -= stackAmt;
                                    }
                                    if (remaining <= 0) break;
                                }
                            }
                            if (remaining <= 0) break;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            SeriaJobs.getInstance().getLogger().warning("Error withdrawing from SeriaMagicBundle virtual storage: " + t.getMessage());
        }

        return amountNeeded - remaining;
    }

    private boolean itemKeyMatchesOrder(String itemKey, JobOrder order) {
        if (itemKey == null || order == null) return false;
        String cleanKey = itemKey.toLowerCase().trim();
        String source = order.getSource() != null ? order.getSource().toUpperCase() : "VANILLA";

        if (source.equals("MMOITEMS")) {
            String expected = ("mmoitems:" + order.getMmoItemType() + ":" + order.getMmoItemId()).toLowerCase();
            return cleanKey.equals(expected);
        }
        if (source.equals("CUSTOMFISHING") || source.equals("FISHING")) {
            for (String cid : order.getCustomItemIds()) {
                String expected = ("customfishing:" + cid).toLowerCase();
                if (cleanKey.equals(expected) || cleanKey.endsWith(":" + cid.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }
        if (source.equals("VANILLA")) {
            if (order.getMaterial() != null) {
                String matName = order.getMaterial().name().toLowerCase();
                return cleanKey.equals("minecraft:" + matName) 
                        || cleanKey.equals(matName)
                        || cleanKey.endsWith(":" + matName);
            }
        }
        for (String cid : order.getCustomItemIds()) {
            String lower = cid.toLowerCase();
            if (cleanKey.contains(":" + lower) || cleanKey.equals(lower)) {
                return true;
            }
        }
        return false;
    }

    // ==========================================
    // PHYSICAL BUNDLE ITEM INTEGRATION
    // ==========================================

    public boolean isMagicBundle(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        String typeName = item.getType().name();
        if (typeName.equals("BUNDLE") || typeName.endsWith("_BUNDLE")) {
            return true;
        }

        if (item.hasItemMeta()) {
            if (item.getItemMeta() instanceof BundleMeta) {
                return true;
            }
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            for (var key : pdc.getKeys()) {
                String ns = key.getNamespace().toLowerCase();
                if (ns.contains("magicbundle") || ns.contains("magic_bundle")) {
                    return true;
                }
            }
        }

        return false;
    }

    @NotNull
    public List<ItemStack> getBundleContents(@Nullable ItemStack bundle) {
        List<ItemStack> contents = new ArrayList<>();
        if (bundle == null || !isMagicBundle(bundle)) return contents;

        // 1. Standard Bukkit BundleMeta
        try {
            if (bundle.getItemMeta() instanceof BundleMeta bundleMeta) {
                List<ItemStack> items = bundleMeta.getItems();
                if (items != null && !items.isEmpty()) {
                    for (ItemStack is : items) {
                        if (is != null && is.getType() != Material.AIR && is.getAmount() > 0) {
                            contents.add(is.clone());
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        if (!contents.isEmpty()) {
            return contents;
        }

        // 2. Paper DataComponent fallback
        try {
            Class<?> dctClass = Class.forName("io.papermc.paper.datacomponent.DataComponentTypes");
            Object bct = dctClass.getField("BUNDLE_CONTENTS").get(null);
            Method hasDataMethod = bundle.getClass().getMethod("hasData", bct.getClass().getInterfaces()[0]);
            if ((boolean) hasDataMethod.invoke(bundle, bct)) {
                Method getDataMethod = bundle.getClass().getMethod("getData", bct.getClass().getInterfaces()[0]);
                Object bc = getDataMethod.invoke(bundle, bct);
                if (bc != null) {
                    Method contentsMethod = bc.getClass().getMethod("contents");
                    Iterable<?> iterable = (Iterable<?>) contentsMethod.invoke(bc);
                    for (Object obj : iterable) {
                        if (obj instanceof ItemStack is && is.getType() != Material.AIR && is.getAmount() > 0) {
                            contents.add(is.clone());
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        return contents;
    }

    public boolean setBundleContents(@NotNull ItemStack bundle, @NotNull List<ItemStack> newContents) {
        if (!isMagicBundle(bundle)) return false;

        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack is : newContents) {
            if (is != null && is.getType() != Material.AIR && is.getAmount() > 0) {
                filtered.add(is);
            }
        }

        boolean success = false;

        // 1. Standard Bukkit BundleMeta
        try {
            if (bundle.getItemMeta() instanceof BundleMeta bundleMeta) {
                bundleMeta.setItems(filtered);
                bundle.setItemMeta(bundleMeta);
                success = true;
            }
        } catch (Throwable ignored) {}

        // 2. Paper DataComponent
        try {
            Class<?> dctClass = Class.forName("io.papermc.paper.datacomponent.DataComponentTypes");
            Class<?> bcClass = Class.forName("io.papermc.paper.datacomponent.item.BundleContents");
            Object bct = dctClass.getField("BUNDLE_CONTENTS").get(null);
            Method builderMethod = bcClass.getMethod("bundleContents", Iterable.class);
            Object newBc = builderMethod.invoke(null, filtered);
            Method setDataMethod = bundle.getClass().getMethod("setData", bct.getClass().getInterfaces()[0], Object.class);
            setDataMethod.invoke(bundle, bct, newBc);
            success = true;
        } catch (Throwable ignored) {}

        return success;
    }

    @Nullable
    public ItemStack generateItem(@Nullable String id, int amount) {
        if (!isAvailable() || id == null || amount <= 0) return null;

        try {
            Class<?> managerClass = Class.forName("id.seria.magicbundle.BundleManager");
            Method getBundle = managerClass.getMethod("getBundle", String.class);
            Object res = getBundle.invoke(null, id);
            if (res instanceof ItemStack is) {
                ItemStack clone = is.clone();
                clone.setAmount(amount);
                return clone;
            }
        } catch (Throwable ignored) {}

        return null;
    }

    public boolean matches(@Nullable ItemStack item, @Nullable String expectedId) {
        if (item == null || expectedId == null) return false;

        try {
            if (item.hasItemMeta()) {
                var pdc = item.getItemMeta().getPersistentDataContainer();
                for (var key : pdc.getKeys()) {
                    String ns = key.getNamespace().toLowerCase();
                    if (ns.contains("magicbundle") || ns.contains("magic_bundle")) {
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
