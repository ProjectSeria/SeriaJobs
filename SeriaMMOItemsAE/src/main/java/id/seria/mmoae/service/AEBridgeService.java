package id.seria.mmoae.service;

import id.seria.mmoae.hook.AEHook;
import id.seria.mmoae.hook.MMOItemsHook;
import id.seria.mmoae.stat.AEEnchantsStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AEBridgeService {

    private final boolean debug;
    private final Logger logger = Logger.getLogger("SeriaMMOItemsAE");

    public AEBridgeService(boolean debug) {
        this.debug = debug;
    }

    /**
     * Inspects an ItemStack, checks if it is an MMOItem with configured 'ae-enchants',
     * and applies any missing or upgraded AE enchantments.
     *
     * @param item The item to process.
     * @return The updated ItemStack with AE abilities, compound NBT, and lore applied.
     */
    public ItemStack processItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return item;
        }

        if (!MMOItemsHook.isAvailable() || !MMOItemsHook.isMMOItem(item)) {
            return item;
        }

        // 1. Resolve any unparsed placeholders / SeriaCore tags in item display name or lore
        boolean metaModified = id.seria.mmoae.util.PlaceholderHelper.processItemMeta(item, null);
        if (metaModified && debug) {
            logger.info("[SeriaMMOItemsAE] Resolved placeholders on item: " +
                    MMOItemsHook.getType(item) + ":" + MMOItemsHook.getId(item));
        }

        if (!AEHook.isAvailable()) {
            return item;
        }

        List<String> aeEnchants = MMOItemsHook.getAEEnchants(item);
        if (aeEnchants == null || aeEnchants.isEmpty()) {
            return item;
        }

        // Check current AE enchants on item
        Map<String, Integer> currentAE = AEHook.getEnchantments(item);
        boolean needsUpdate = false;

        for (String spec : aeEnchants) {
            if (spec == null || spec.trim().isEmpty()) continue;
            String[] tokens = spec.trim().split("\\s+");
            String name = tokens[0].toLowerCase();
            int targetLevel = 1;
            if (tokens.length >= 2) {
                try {
                    targetLevel = Integer.parseInt(tokens[1]);
                } catch (NumberFormatException ignored) {}
            }

            Integer currentLvl = currentAE.get(name);
            if (currentLvl == null || currentLvl < targetLevel) {
                needsUpdate = true;
                break;
            }
        }

        if (!needsUpdate) {
            return item;
        }

        if (debug) {
            logger.info("[SeriaMMOItemsAE] Applying " + aeEnchants.size() + " AE enchant(s) to " +
                    MMOItemsHook.getType(item) + ":" + MMOItemsHook.getId(item));
        }

        // Apply enchants through official AdvancedEnchantments API
        ItemStack modified = AEHook.applyEnchantList(item, aeEnchants);

        // Ensure NBT tag is also preserved on the newly created ItemStack
        try {
            NBTItem nbt = NBTItem.get(modified);
            if (!nbt.hasTag(AEEnchantsStat.NBT_TAG)) {
                modified = nbt.addTag(new ItemTag(AEEnchantsStat.NBT_TAG, String.join(";;", aeEnchants))).toItem();
            }
        } catch (Throwable ignored) {}

        // Ensure display name is also cleaned up on modified item
        id.seria.mmoae.util.PlaceholderHelper.processItemMeta(modified, null);

        return modified;
    }

    /**
     * Checks if all configured AE enchants are already applied on the item and placeholders resolved.
     */
    public boolean isFullyEnchanted(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return true;
        }

        if (!MMOItemsHook.isAvailable() || !MMOItemsHook.isMMOItem(item)) {
            return true;
        }

        // Check if item still has unparsed placeholder tags in display name
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName();
            if (id.seria.mmoae.util.PlaceholderHelper.hasPlaceholders(name)) {
                return false;
            }
        }

        if (!AEHook.isAvailable()) {
            return true;
        }

        List<String> aeEnchants = MMOItemsHook.getAEEnchants(item);
        if (aeEnchants == null || aeEnchants.isEmpty()) {
            return true;
        }

        Map<String, Integer> currentAE = AEHook.getEnchantments(item);
        for (String spec : aeEnchants) {
            if (spec == null || spec.trim().isEmpty()) continue;
            String[] tokens = spec.trim().split("\\s+");
            String name = tokens[0].toLowerCase();
            int targetLevel = 1;
            if (tokens.length >= 2) {
                try {
                    targetLevel = Integer.parseInt(tokens[1]);
                } catch (NumberFormatException ignored) {}
            }

            Integer currentLvl = currentAE.get(name);
            if (currentLvl == null || currentLvl < targetLevel) {
                return false;
            }
        }

        return true;
    }
}
