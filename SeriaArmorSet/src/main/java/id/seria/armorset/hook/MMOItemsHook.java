package id.seria.armorset.hook;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.config.ArmorSetConfig;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.inventory.EquippedItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MMOItemsHook {

    private static final String SET_NBT_TAG = "MMOITEMS_ITEM_SET";

    private final SeriaArmorSetPlugin plugin;

    public MMOItemsHook(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasActiveSet(@NotNull Player player) {
        return getActiveSet(player) != null;
    }

    public @Nullable String getActiveSet(@NotNull Player player) {
        ArmorSetConfig config = plugin.getConfiguration();
        Map<String, Integer> configuredSets = config.getSets();
        if (configuredSets.isEmpty()) return null;

        Map<String, Integer> wornCounts = getWornSetCounts(player);

        for (Map.Entry<String, Integer> entry : configuredSets.entrySet()) {
            String setId = entry.getKey();
            int required = entry.getValue();
            int worn = wornCounts.getOrDefault(setId, 0);

            if (worn >= required) {
                if (config.isDebug()) {
                    plugin.getLogger().info("[Debug] " + player.getName() + " has active telepathy set: "
                            + setId + " (" + worn + "/" + required + " pieces)");
                }
                return setId;
            }
        }

        return null;
    }

    public int getPieceCount(@NotNull Player player, @NotNull String setId) {
        Map<String, Integer> counts = getWornSetCounts(player);
        return counts.getOrDefault(setId.toUpperCase(Locale.ROOT), 0);
    }

    public @NotNull Map<String, Integer> getWornSetCounts(@NotNull Player player) {
        ArmorSetConfig config = plugin.getConfiguration();
        boolean armorOnly = config.isArmorSlotsOnly();

        Map<String, Integer> apiCounts = tryGetCountsFromMMOItemsAPI(player, armorOnly);
        if (!apiCounts.isEmpty()) {
            return apiCounts;
        }

        return getCountsFromArmorNBT(player);
    }

    private @NotNull Map<String, Integer> tryGetCountsFromMMOItemsAPI(@NotNull Player player, boolean armorOnly) {
        try {
            if (!PlayerData.has(player)) {
                return Collections.emptyMap();
            }

            PlayerData playerData = PlayerData.get(player);
            if (playerData == null || playerData.getInventory() == null) {
                return Collections.emptyMap();
            }

            Set<EquippedItem> equippedItems = playerData.getInventory().getEquipped();
            if (equippedItems == null || equippedItems.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<String, Integer> counts = new HashMap<>();
            for (EquippedItem item : equippedItems) {
                if (item == null) continue;

                if (armorOnly) {
                    EquipmentSlot slot = item.getEquipmentSlot();
                    if (slot == null || !isArmorSlot(slot)) {
                        continue;
                    }
                }

                ItemSet set = item.getSet();
                if (set != null && set.getId() != null) {
                    String id = set.getId().trim().toUpperCase(Locale.ROOT);
                    counts.merge(id, 1, Integer::sum);
                }
            }

            return counts;
        } catch (Throwable t) {
            if (plugin.getConfiguration().isDebug()) {
                plugin.getLogger().warning("[Debug] MMOItems API resolution error: " + t.getMessage() + ", using NBT fallback.");
            }
            return Collections.emptyMap();
        }
    }

    private boolean isArmorSlot(@NotNull EquipmentSlot slot) {
        return slot.isBody()
                || slot == EquipmentSlot.ARMOR
                || slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }

    private @NotNull Map<String, Integer> getCountsFromArmorNBT(@NotNull Player player) {
        Map<String, Integer> counts = new HashMap<>();
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        for (ItemStack piece : armorContents) {
            if (piece == null || piece.getType().isAir()) continue;

            try {
                NBTItem nbt = NBTItem.get(piece);
                if (nbt != null && nbt.hasTag(SET_NBT_TAG)) {
                    String setId = nbt.getString(SET_NBT_TAG);
                    if (setId != null && !setId.trim().isEmpty()) {
                        counts.merge(setId.trim().toUpperCase(Locale.ROOT), 1, Integer::sum);
                    }
                }
            } catch (Throwable t) {
                if (plugin.getConfiguration().isDebug()) {
                    plugin.getLogger().warning("[Debug] Failed reading NBT on item: " + t.getMessage());
                }
            }
        }

        return counts;
    }
}