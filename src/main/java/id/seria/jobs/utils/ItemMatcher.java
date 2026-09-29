package id.seria.jobs.utils;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.hook.SeriaMagicBundleHook;
import id.seria.jobs.model.JobOrder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ItemMatcher {

    public static boolean matches(@Nullable ItemStack item, @NotNull JobOrder order) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        String source = order.getSource() != null ? order.getSource().toUpperCase() : "VANILLA";

        switch (source) {
            case "MMOITEMS":
                if (!SeriaJobs.getInstance().getHookManager().isMmoItemsLoaded()) {
                    return false;
                }
                return SeriaJobs.getInstance().getHookManager().getMmoItemsHook()
                        .matches(item, order.getMmoItemType(), order.getMmoItemId());

            case "MYTHICMOBS":
            case "MYTHIC":
                if (!SeriaJobs.getInstance().getHookManager().isMythicMobsLoaded()) {
                    return false;
                }
                for (String mId : order.getCustomItemIds()) {
                    if (SeriaJobs.getInstance().getHookManager().getMythicMobsHook().matches(item, mId)) {
                        return true;
                    }
                }
                return false;

            case "CUSTOMFISHING":
            case "FISHING":
                if (!SeriaJobs.getInstance().getHookManager().isCustomFishingLoaded()) {
                    return false;
                }
                for (String fId : order.getCustomItemIds()) {
                    if (SeriaJobs.getInstance().getHookManager().getCustomFishingHook().matches(item, fId)) {
                        return true;
                    }
                }
                return false;

            case "SERIAMAGICBUNDLE":
            case "MAGICBUNDLE":
                if (!SeriaJobs.getInstance().getHookManager().isSeriaMagicBundleLoaded()) {
                    return false;
                }
                for (String bId : order.getCustomItemIds()) {
                    if (SeriaJobs.getInstance().getHookManager().getSeriaMagicBundleHook().matches(item, bId)) {
                        return true;
                    }
                }
                return false;

            case "SERIACRAFTING":
            case "CRAFTING":
                if (!SeriaJobs.getInstance().getHookManager().isSeriaCraftingLoaded()) {
                    return false;
                }
                for (String cId : order.getCustomItemIds()) {
                    if (SeriaJobs.getInstance().getHookManager().getSeriaCraftingHook().matches(item, cId)) {
                        return true;
                    }
                }
                return false;

            case "VANILLA":
            default:
                // Vanilla Item Matching
                if (order.getMaterial() == null || item.getType() != order.getMaterial()) {
                    return false;
                }

                ItemMeta meta = item.getItemMeta();

                // Custom Model Data check if configured
                if (order.getCustomModelData() != null) {
                    if (meta == null || !meta.hasCustomModelData() || meta.getCustomModelData() != order.getCustomModelData()) {
                        return false;
                    }
                }

                // Strict NBT check
                if (order.isStrictNbt()) {
                    if (meta != null && (meta.hasDisplayName() || meta.hasLore() || meta.hasEnchants())) {
                        return false;
                    }
                }

                return true;
        }
    }

    public static boolean isBundleOrder(@NotNull JobOrder order) {
        String source = order.getSource() != null ? order.getSource().toUpperCase() : "VANILLA";
        return source.equals("SERIAMAGICBUNDLE") || source.equals("MAGICBUNDLE");
    }

    public static boolean isBundleItem(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String typeName = item.getType().name();
        if (typeName.equals("BUNDLE") || typeName.endsWith("_BUNDLE")) {
            return true;
        }
        if (item.hasItemMeta() && item.getItemMeta() instanceof BundleMeta) {
            return true;
        }
        SeriaMagicBundleHook hook = SeriaJobs.getInstance().getHookManager().getSeriaMagicBundleHook();
        if (hook != null && hook.isMagicBundle(item)) {
            return true;
        }
        return false;
    }

    public static int countMatchingItems(@NotNull Player player, @NotNull JobOrder order) {
        int looseCount = 0;
        ItemStack[] contents = player.getInventory().getStorageContents();

        for (ItemStack item : contents) {
            if (item == null) continue;
            // Count loose matching items
            if (!isBundleItem(item) && matches(item, order)) {
                looseCount += item.getAmount();
            }
        }

        int virtualCount = 0;
        int bundleCount = 0;

        // If this order is not for the bundle itself, also count items in Magic Bundles & sacks!
        if (!isBundleOrder(order)) {
            // 1. Virtual SeriaMagicBundle storage (sacks / database)
            SeriaMagicBundleHook hook = SeriaJobs.getInstance().getHookManager().getSeriaMagicBundleHook();
            if (hook != null && hook.isAvailable()) {
                virtualCount = hook.countVirtualStorageItems(player, order);
            }

            // 2. Physical Magic Bundles in player inventory / offhand
            bundleCount = countItemsInBundles(player, order);
        }

        int total = looseCount + virtualCount + bundleCount;
        SeriaJobs.getInstance().getLogger().info("[SeriaJobs] Deposit check for " + player.getName() 
                + " (Order: " + order.getId() + "): loose=" + looseCount 
                + ", physicalBundle=" + bundleCount 
                + ", virtualSack=" + virtualCount 
                + " => total=" + total);

        return total;
    }

    private static int countItemsInBundles(@NotNull Player player, @NotNull JobOrder order) {
        int total = 0;
        SeriaMagicBundleHook hook = SeriaJobs.getInstance().getHookManager().getSeriaMagicBundleHook();

        // 1. Storage contents
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (isBundleItem(item)) {
                List<ItemStack> bundleContents = hook != null ? hook.getBundleContents(item) : getFallbackBundleContents(item);
                for (ItemStack inner : bundleContents) {
                    if (matches(inner, order)) {
                        total += inner.getAmount();
                    }
                }
            }
        }

        // 2. Offhand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (isBundleItem(offhand)) {
            List<ItemStack> bundleContents = hook != null ? hook.getBundleContents(offhand) : getFallbackBundleContents(offhand);
            for (ItemStack inner : bundleContents) {
                if (matches(inner, order)) {
                    total += inner.getAmount();
                }
            }
        }

        return total;
    }

    public static int removeMatchingItems(@NotNull Player player, @NotNull JobOrder order, int targetAmount) {
        int remainingNeeded = targetAmount;
        ItemStack[] contents = player.getInventory().getStorageContents();

        // 1. Remove from loose inventory first
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) continue;
            if (!isBundleOrder(order) && isBundleItem(item)) continue;

            if (matches(item, order)) {
                int currentAmount = item.getAmount();
                if (currentAmount <= remainingNeeded) {
                    player.getInventory().setItem(i, null);
                    remainingNeeded -= currentAmount;
                } else {
                    item.setAmount(currentAmount - remainingNeeded);
                    remainingNeeded = 0;
                }
            }
            if (remainingNeeded <= 0) {
                break;
            }
        }

        // 2. If more items are still needed and order is not for the bundle itself, pull from Magic Bundles!
        if (remainingNeeded > 0 && !isBundleOrder(order)) {
            // 2a. Check physical bundles in inventory/offhand first
            remainingNeeded = removeItemsFromBundles(player, order, remainingNeeded);

            // 2b. If still needed, pull from SeriaMagicBundle virtual storage (sacks)!
            if (remainingNeeded > 0) {
                SeriaMagicBundleHook hook = SeriaJobs.getInstance().getHookManager().getSeriaMagicBundleHook();
                if (hook != null && hook.isAvailable()) {
                    int withdrawn = hook.withdrawVirtualStorageItems(player, order, remainingNeeded);
                    remainingNeeded -= withdrawn;
                }
            }
        }

        player.updateInventory();
        return targetAmount - remainingNeeded;
    }

    private static int removeItemsFromBundles(@NotNull Player player, @NotNull JobOrder order, int targetAmount) {
        int remainingNeeded = targetAmount;
        SeriaMagicBundleHook hook = SeriaJobs.getInstance().getHookManager().getSeriaMagicBundleHook();

        // 1. Storage contents
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack bundle = contents[i];
            if (isBundleItem(bundle)) {
                List<ItemStack> bundleContents = hook != null ? hook.getBundleContents(bundle) : getFallbackBundleContents(bundle);
                boolean changed = false;

                for (ItemStack inner : bundleContents) {
                    if (matches(inner, order)) {
                        int currentAmount = inner.getAmount();
                        if (currentAmount <= remainingNeeded) {
                            remainingNeeded -= currentAmount;
                            inner.setAmount(0);
                            changed = true;
                        } else {
                            inner.setAmount(currentAmount - remainingNeeded);
                            remainingNeeded = 0;
                            changed = true;
                        }
                    }
                    if (remainingNeeded <= 0) break;
                }

                if (changed) {
                    boolean updated = false;
                    if (hook != null) {
                        updated = hook.setBundleContents(bundle, bundleContents);
                    }
                    if (!updated) {
                        setFallbackBundleContents(bundle, bundleContents);
                    }
                    player.getInventory().setItem(i, bundle);
                }
            }
            if (remainingNeeded <= 0) break;
        }

        // 2. Offhand if still needed
        if (remainingNeeded > 0) {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (isBundleItem(offhand)) {
                List<ItemStack> bundleContents = hook != null ? hook.getBundleContents(offhand) : getFallbackBundleContents(offhand);
                boolean changed = false;

                for (ItemStack inner : bundleContents) {
                    if (matches(inner, order)) {
                        int currentAmount = inner.getAmount();
                        if (currentAmount <= remainingNeeded) {
                            remainingNeeded -= currentAmount;
                            inner.setAmount(0);
                            changed = true;
                        } else {
                            inner.setAmount(currentAmount - remainingNeeded);
                            remainingNeeded = 0;
                            changed = true;
                        }
                    }
                    if (remainingNeeded <= 0) break;
                }

                if (changed) {
                    boolean updated = false;
                    if (hook != null) {
                        updated = hook.setBundleContents(offhand, bundleContents);
                    }
                    if (!updated) {
                        setFallbackBundleContents(offhand, bundleContents);
                    }
                    player.getInventory().setItemInOffHand(offhand);
                }
            }
        }

        return remainingNeeded;
    }

    private static List<ItemStack> getFallbackBundleContents(ItemStack bundle) {
        List<ItemStack> list = new ArrayList<>();
        if (bundle == null) return list;

        // 1. Standard BundleMeta
        try {
            if (bundle.getItemMeta() instanceof BundleMeta bm) {
                for (ItemStack is : bm.getItems()) {
                    if (is != null && is.getType() != Material.AIR && is.getAmount() > 0) {
                        list.add(is.clone());
                    }
                }
            }
        } catch (Throwable ignored) {}

        if (!list.isEmpty()) return list;

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
                            list.add(is.clone());
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        return list;
    }

    private static void setFallbackBundleContents(ItemStack bundle, List<ItemStack> contents) {
        if (bundle == null) return;
        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack is : contents) {
            if (is != null && is.getType() != Material.AIR && is.getAmount() > 0) {
                filtered.add(is);
            }
        }

        try {
            if (bundle.getItemMeta() instanceof BundleMeta bm) {
                bm.setItems(filtered);
                bundle.setItemMeta(bm);
            }
        } catch (Throwable ignored) {}

        try {
            Class<?> dctClass = Class.forName("io.papermc.paper.datacomponent.DataComponentTypes");
            Class<?> bcClass = Class.forName("io.papermc.paper.datacomponent.item.BundleContents");
            Object bct = dctClass.getField("BUNDLE_CONTENTS").get(null);
            Method builderMethod = bcClass.getMethod("bundleContents", Iterable.class);
            Object newBc = builderMethod.invoke(null, filtered);
            Method setDataMethod = bundle.getClass().getMethod("setData", bct.getClass().getInterfaces()[0], Object.class);
            setDataMethod.invoke(bundle, bct, newBc);
        } catch (Throwable ignored) {}
    }

    public static String getItemDisplayName(@NotNull JobOrder order) {
        String source = order.getSource() != null ? order.getSource().toUpperCase() : "VANILLA";
        switch (source) {
            case "MMOITEMS":
                return "MMOItem (" + order.getMmoItemId() + ")";
            case "MYTHICMOBS":
            case "MYTHIC":
                if (order.getCustomItemIds().size() > 1) {
                    return "MythicItem (" + order.getCustomItemIds().size() + " items)";
                }
                return "MythicItem (" + order.getCustomItemId() + ")";
            case "CUSTOMFISHING":
            case "FISHING":
                if (order.getCustomItemIds().size() > 1) {
                    return "CustomFishing (" + order.getCustomItemIds().size() + " items)";
                }
                return "CustomFishing (" + order.getCustomItemId() + ")";
            case "SERIAMAGICBUNDLE":
            case "MAGICBUNDLE":
                if (order.getCustomItemIds().size() > 1) {
                    return "MagicBundle (" + order.getCustomItemIds().size() + " items)";
                }
                return "MagicBundle (" + order.getCustomItemId() + ")";
            case "SERIACRAFTING":
            case "CRAFTING":
                if (order.getCustomItemIds().size() > 1) {
                    return "CraftedItem (" + order.getCustomItemIds().size() + " items)";
                }
                return "CraftedItem (" + order.getCustomItemId() + ")";
            case "VANILLA":
            default:
                if (order.getMaterial() != null) {
                    String name = order.getMaterial().name().replace('_', ' ').toLowerCase();
                    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
                }
                return "Unknown Item";
        }
    }
}
