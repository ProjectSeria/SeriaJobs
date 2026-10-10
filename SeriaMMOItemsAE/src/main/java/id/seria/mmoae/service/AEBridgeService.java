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

        // 1. Resolve unparsed placeholders in item display name ONLY (Lore is never touched)
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String rawName = item.getItemMeta().getDisplayName();
            if (id.seria.mmoae.util.PlaceholderHelper.hasPlaceholders(rawName)) {
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                String parsed = id.seria.mmoae.util.PlaceholderHelper.parseToLegacy(null, rawName);
                meta.setDisplayName(parsed);
                try {
                    meta.displayName(id.seria.mmoae.util.PlaceholderHelper.parseToComponent(null, rawName));
                } catch (Throwable ignored) {}
                item.setItemMeta(meta);
            }
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

            Integer currentLvl = null;
            for (String candidate : AEHook.getCandidateNames(name)) {
                currentLvl = currentAE.get(candidate);
                if (currentLvl != null) break;
            }

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

        // Capture complete original lore to ensure zero loss of MMOItems stats / lore lines
        List<net.kyori.adventure.text.Component> origComp = (item.hasItemMeta() && item.getItemMeta().hasLore())
                ? item.getItemMeta().lore() : null;
        List<String> origLegacy = (item.hasItemMeta() && item.getItemMeta().hasLore())
                ? item.getItemMeta().getLore() : null;

        // Apply enchants through official AdvancedEnchantments API
        ItemStack modified = AEHook.applyEnchantList(item, aeEnchants);

        // Ensure NBT tag is always updated to match current template specifications
        try {
            NBTItem nbt = NBTItem.get(modified);
            modified = nbt.addTag(new ItemTag(AEEnchantsStat.NBT_TAG, String.join(";;", aeEnchants))).toItem();
        } catch (Throwable ignored) {}

        // Guarantee that MMOItems native lore (Status, stats, and description) is never truncated by AE
        modified = preserveAndMergeLore(origComp, origLegacy, modified);

        // Adjust spacing: remove space before AE enchant, add space after AE enchant
        modified = adjustAELoreSpacing(modified, aeEnchants);

        return modified;
    }

    /**
     * Adjusts spacing around the AE enchantment block in lore:
     * - Removes any blank/empty lines immediately before the first AE enchant.
     * - Adds exactly one blank/empty line immediately after the last AE enchant before subsequent sections.
     */
    public ItemStack adjustAELoreSpacing(ItemStack item, List<String> aeSpecs) {
        if (item == null || !item.hasItemMeta() || aeSpecs == null || aeSpecs.isEmpty()) {
            return item;
        }

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return item;
        }

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return item;
        }

        // Build normalized set of AE enchant identifiers
        java.util.Set<String> cleanNames = new java.util.HashSet<>();
        for (String spec : aeSpecs) {
            if (spec == null || spec.trim().isEmpty()) continue;
            String name = spec.trim().split("\\s+")[0].toLowerCase();
            cleanNames.add(name.replace("_", "").replace("-", ""));
            for (String cand : AEHook.getCandidateNames(name)) {
                cleanNames.add(cand.replace("_", "").replace("-", ""));
            }
        }

        int firstAEIndex = -1;
        int lastAEIndex = -1;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line == null) continue;
            String stripped = org.bukkit.ChatColor.stripColor(line).toLowerCase().replaceAll("[^a-z0-9]", "");
            if (stripped.isEmpty()) continue;

            for (String name : cleanNames) {
                if (!name.isEmpty() && stripped.startsWith(name)) {
                    if (firstAEIndex == -1) {
                        firstAEIndex = i;
                    }
                    lastAEIndex = i;
                    break;
                }
            }
        }

        if (firstAEIndex == -1) {
            return item;
        }

        List<String> updatedLore = new java.util.ArrayList<>(lore);

        // 1. Remove space before the AE enchant (any empty line immediately preceding firstAEIndex)
        while (firstAEIndex > 0) {
            String prev = updatedLore.get(firstAEIndex - 1);
            if (prev == null || org.bukkit.ChatColor.stripColor(prev).trim().isEmpty()) {
                updatedLore.remove(firstAEIndex - 1);
                firstAEIndex--;
                lastAEIndex--;
            } else {
                break;
            }
        }

        // 2. Add space after AE enchant (ensure exactly one empty line immediately after lastAEIndex)
        if (lastAEIndex + 1 < updatedLore.size()) {
            String next = updatedLore.get(lastAEIndex + 1);
            if (next == null || org.bukkit.ChatColor.stripColor(next).trim().isEmpty()) {
                // If there are multiple empty lines, collapse to exactly one
                while (lastAEIndex + 2 < updatedLore.size()) {
                    String following = updatedLore.get(lastAEIndex + 2);
                    if (following == null || org.bukkit.ChatColor.stripColor(following).trim().isEmpty()) {
                        updatedLore.remove(lastAEIndex + 2);
                    } else {
                        break;
                    }
                }
            } else {
                // Insert one blank line right after AE enchant block
                updatedLore.add(lastAEIndex + 1, "");
            }
        }

        meta.setLore(updatedLore);
        try {
            // Also synchronize adventure lore components if available
            List<net.kyori.adventure.text.Component> compLore = new java.util.ArrayList<>();
            for (String line : updatedLore) {
                if (line == null || line.isEmpty()) {
                    compLore.add(net.kyori.adventure.text.Component.empty());
                } else {
                    compLore.add(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(line));
                }
            }
            meta.lore(compLore);
        } catch (Throwable ignored) {}

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Checks if the spacing before and after AE enchants is already correct.
     */
    public boolean isLoreSpacingCorrect(ItemStack item, List<String> aeSpecs) {
        if (item == null || !item.hasItemMeta() || aeSpecs == null || aeSpecs.isEmpty()) {
            return true;
        }
        if (!item.getItemMeta().hasLore()) {
            return true;
        }

        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) {
            return true;
        }

        java.util.Set<String> cleanNames = new java.util.HashSet<>();
        for (String spec : aeSpecs) {
            if (spec == null || spec.trim().isEmpty()) continue;
            String name = spec.trim().split("\\s+")[0].toLowerCase();
            cleanNames.add(name.replace("_", "").replace("-", ""));
            for (String cand : AEHook.getCandidateNames(name)) {
                cleanNames.add(cand.replace("_", "").replace("-", ""));
            }
        }

        int firstAEIndex = -1;
        int lastAEIndex = -1;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line == null) continue;
            String stripped = org.bukkit.ChatColor.stripColor(line).toLowerCase().replaceAll("[^a-z0-9]", "");
            if (stripped.isEmpty()) continue;

            for (String name : cleanNames) {
                if (!name.isEmpty() && stripped.startsWith(name)) {
                    if (firstAEIndex == -1) {
                        firstAEIndex = i;
                    }
                    lastAEIndex = i;
                    break;
                }
            }
        }

        if (firstAEIndex == -1) {
            return true;
        }

        // Check before firstAEIndex
        if (firstAEIndex > 0) {
            String prev = lore.get(firstAEIndex - 1);
            if (prev == null || org.bukkit.ChatColor.stripColor(prev).trim().isEmpty()) {
                return false; // Still has space before AE enchant
            }
        }

        // Check after lastAEIndex
        if (lastAEIndex + 1 < lore.size()) {
            String next = lore.get(lastAEIndex + 1);
            if (next != null && !org.bukkit.ChatColor.stripColor(next).trim().isEmpty()) {
                return false; // Missing space after AE enchant
            }
        }

        return true;
    }

    /**
     * Ensures that all original MMOItems lore lines (Status, Attack Damage, Attack Speed, etc.)
     * are preserved alongside any newly added AE enchantment lines.
     */
    private ItemStack preserveAndMergeLore(List<net.kyori.adventure.text.Component> origComp,
                                          List<String> origLegacy,
                                          ItemStack modified) {
        if (modified == null || !modified.hasItemMeta()) {
            return modified;
        }

        if (origComp == null && origLegacy == null) {
            return modified;
        }

        List<String> afterLegacy = modified.getItemMeta().getLore();
        if (afterLegacy == null || afterLegacy.isEmpty()) {
            org.bukkit.inventory.meta.ItemMeta meta = modified.getItemMeta();
            if (origComp != null) {
                meta.lore(origComp);
            } else {
                meta.setLore(origLegacy);
            }
            modified.setItemMeta(meta);
            return modified;
        }

        // Check if all original non-empty lines are still present in after's lore
        boolean missingOriginal = false;
        if (origLegacy != null) {
            for (String origLine : origLegacy) {
                if (origLine != null && !origLine.trim().isEmpty() && !afterLegacy.contains(origLine)) {
                    missingOriginal = true;
                    break;
                }
            }
        }

        if (missingOriginal && origLegacy != null) {
            // Find newly added AE enchant lines
            java.util.List<String> aeLines = new java.util.ArrayList<>();
            for (String line : afterLegacy) {
                if (!origLegacy.contains(line)) {
                    aeLines.add(line);
                }
            }

            // Find best insertion point (right before "Status" or right after native Bukkit enchants)
            int insertIndex = 0;
            for (int i = 0; i < origLegacy.size(); i++) {
                String line = origLegacy.get(i);
                if (line != null && (line.toLowerCase().contains("status") || line.contains("---"))) {
                    insertIndex = i;
                    break;
                }
            }

            java.util.List<String> merged = new java.util.ArrayList<>(origLegacy.size() + aeLines.size());
            merged.addAll(origLegacy.subList(0, insertIndex));
            merged.addAll(aeLines);
            merged.addAll(origLegacy.subList(insertIndex, origLegacy.size()));

            org.bukkit.inventory.meta.ItemMeta meta = modified.getItemMeta();
            meta.setLore(merged);
            modified.setItemMeta(meta);
        }

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

            Integer currentLvl = null;
            for (String candidate : AEHook.getCandidateNames(name)) {
                currentLvl = currentAE.get(candidate);
                if (currentLvl != null) break;
            }

            if (currentLvl == null || currentLvl < targetLevel) {
                return false;
            }
        }

        if (!isLoreSpacingCorrect(item, aeEnchants)) {
            return false;
        }

        return true;
    }
}
