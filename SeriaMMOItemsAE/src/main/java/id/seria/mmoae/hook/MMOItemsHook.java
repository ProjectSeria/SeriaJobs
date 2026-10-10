package id.seria.mmoae.hook;

import id.seria.mmoae.stat.AEEnchantsStat;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.StringListData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MMOItemsHook {

    private static boolean available = false;
    private static AEEnchantsStat statInstance = null;
    private static final Logger LOGGER = Logger.getLogger("SeriaMMOItemsAE");

    public static void initialize() {
        Plugin mmoPlugin = Bukkit.getPluginManager().getPlugin("MMOItems");
        if (mmoPlugin != null && mmoPlugin.isEnabled()) {
            try {
                if (statInstance == null) {
                    statInstance = new AEEnchantsStat();
                    MMOItems.plugin.getStats().register(statInstance);
                }
                available = true;
                LOGGER.info("[SeriaMMOItemsAE] Successfully registered 'AE_ENCHANTS' (ae-enchants) custom stat into MMOItems!");
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[SeriaMMOItemsAE] Failed to register custom stat in MMOItems: " + t.getMessage(), t);
                available = false;
            }
        } else {
            available = false;
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static AEEnchantsStat getStatInstance() {
        return statInstance;
    }

    /**
     * Checks if an ItemStack is an MMOItem.
     */
    public static boolean isMMOItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        try {
            NBTItem nbtItem = NBTItem.get(item);
            return nbtItem.hasType();
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Returns the MMOItem Type ID (e.g., "SWORD").
     */
    public static String getType(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        try {
            NBTItem nbtItem = NBTItem.get(item);
            return nbtItem.getType();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns the MMOItem ID (e.g., "HALLOWEEN2026_FREE_SWORD").
     */
    public static String getId(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        try {
            NBTItem nbtItem = NBTItem.get(item);
            return nbtItem.getString("MMOITEMS_ITEM_ID");
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Retrieves the list of AE enchants defined on the item or its MMOItems template.
     */
    public static List<String> getAEEnchants(ItemStack item) {
        List<String> result = new ArrayList<>();
        if (item == null || item.getType().isAir()) return result;

        try {
            NBTItem nbtItem = NBTItem.get(item);
            if (!nbtItem.hasType()) return result;

            // 1. Check if the item already has the serialized AE_ENCHANTS tag
            if (nbtItem.hasTag(AEEnchantsStat.NBT_TAG)) {
                String raw = nbtItem.getString(AEEnchantsStat.NBT_TAG);
                if (raw != null && !raw.trim().isEmpty()) {
                    String[] split = raw.split(";;");
                    result.addAll(Arrays.asList(split));
                    return result;
                }
            }

            // 2. Retrieve directly from the MMOItem template config
            String typeId = nbtItem.getType();
            String itemId = nbtItem.getString("MMOITEMS_ITEM_ID");
            if (typeId != null && itemId != null) {
                List<String> fromTemplate = getAEEnchantsFromTemplate(typeId, itemId);
                if (!fromTemplate.isEmpty()) {
                    result.addAll(fromTemplate);
                }
            }
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "[SeriaMMOItemsAE] Error reading AE enchants from item: " + t.getMessage());
        }

        return result;
    }

    /**
     * Reads the 'ae-enchants' list directly from the MMOItems template config section.
     */
    public static List<String> getAEEnchantsFromTemplate(String typeId, String itemId) {
        List<String> list = new ArrayList<>();
        if (typeId == null || itemId == null) return list;

        try {
            Type type = MMOItems.plugin.getTypes().get(typeId);
            if (type == null) return list;

            MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, itemId);
            if (template != null && statInstance != null) {
                // Check if template base has stat data
                if (template.getBaseItemData().containsKey(statInstance)) {
                    Object data = template.getBaseItemData().get(statInstance);
                    if (data instanceof StringListData) {
                        list.addAll(((StringListData) data).getList());
                        if (!list.isEmpty()) return list;
                    }
                }
            }

            // Check raw config file of type (handles both Section and List)
            ConfigurationSection config = type.getConfigFile().getConfig();
            if (config != null) {
                String basePath = itemId + ".base.ae-enchants";
                if (config.contains(basePath)) {
                    Object raw = config.get(basePath);
                    list.addAll(AEEnchantsStat.parseRawObject(raw));
                    if (!list.isEmpty()) return list;
                }

                String altPath = itemId + ".ae-enchants";
                if (config.contains(altPath)) {
                    Object raw = config.get(altPath);
                    list.addAll(AEEnchantsStat.parseRawObject(raw));
                    if (!list.isEmpty()) return list;
                }
            }
        } catch (Throwable ignored) {}

        return list;
    }

    /**
     * Pre-processes an MMOItem template, resolving any SeriaCore semantic tags (<sc:...>, <p:...>)
     * or PlaceholderAPI placeholders in its name and lore before items are generated or given.
     *
     * @param template The MMOItemTemplate to pre-process.
     * @return true if the template's name or lore was updated.
     */
    public static boolean preProcessTemplate(MMOItemTemplate template) {
        if (template == null) return false;
        boolean modified = false;

        try {
            // 1. Process Item Name
            if (template.getBaseItemData().containsKey(net.Indyuce.mmoitems.ItemStats.NAME)) {
                net.Indyuce.mmoitems.stat.data.random.RandomStatData data =
                        template.getBaseItemData().get(net.Indyuce.mmoitems.ItemStats.NAME);
                if (data instanceof net.Indyuce.mmoitems.stat.data.StringData strData) {
                    String raw = strData.getString();
                    if (id.seria.mmoae.util.PlaceholderHelper.hasPlaceholders(raw)) {
                        String parsed = id.seria.mmoae.util.PlaceholderHelper.parseToLegacy(null, raw);
                        strData.setString(parsed);
                        template.getBaseItemData().put(net.Indyuce.mmoitems.ItemStats.NAME,
                                net.Indyuce.mmoitems.ItemStats.NAME.whenInitialized(parsed));
                        modified = true;
                    }
                }
            } else {
                // Check if defined in raw config
                Type type = template.getType();
                if (type != null && type.getConfigFile() != null) {
                    ConfigurationSection config = type.getConfigFile().getConfig();
                    if (config != null) {
                        String namePath = template.getId() + ".base.name";
                        if (config.isString(namePath)) {
                            String raw = config.getString(namePath);
                            if (id.seria.mmoae.util.PlaceholderHelper.hasPlaceholders(raw)) {
                                String parsed = id.seria.mmoae.util.PlaceholderHelper.parseToLegacy(null, raw);
                                template.getBaseItemData().put(net.Indyuce.mmoitems.ItemStats.NAME,
                                        net.Indyuce.mmoitems.ItemStats.NAME.whenInitialized(parsed));
                                modified = true;
                            }
                        }
                    }
                }
            }

            // 2. Process Item Lore
            if (template.getBaseItemData().containsKey(net.Indyuce.mmoitems.ItemStats.LORE)) {
                net.Indyuce.mmoitems.stat.data.random.RandomStatData data =
                        template.getBaseItemData().get(net.Indyuce.mmoitems.ItemStats.LORE);
                if (data instanceof StringListData listData) {
                    List<String> list = listData.getList();
                    boolean loreModified = false;
                    List<String> updated = new ArrayList<>(list.size());
                    for (String line : list) {
                        if (id.seria.mmoae.util.PlaceholderHelper.hasPlaceholders(line)) {
                            updated.add(id.seria.mmoae.util.PlaceholderHelper.parseToLegacy(null, line));
                            loreModified = true;
                        } else {
                            updated.add(line);
                        }
                    }
                    if (loreModified) {
                        list.clear();
                        list.addAll(updated);
                        template.getBaseItemData().put(net.Indyuce.mmoitems.ItemStats.LORE,
                                net.Indyuce.mmoitems.ItemStats.LORE.whenInitialized(updated));
                        modified = true;
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "[SeriaMMOItemsAE] Failed to preprocess template " + template.getId() + ": " + t.getMessage());
        }

        return modified;
    }

    /**
     * Scans and pre-processes all registered MMOItems templates across all types,
     * ensuring placeholders in item names and lores are resolved immediately.
     *
     * @return Number of templates successfully updated.
     */
    public static int preProcessAllTemplates() {
        if (!available) return 0;
        int count = 0;
        try {
            for (MMOItemTemplate template : MMOItems.plugin.getTemplates().collectTemplates()) {
                if (preProcessTemplate(template)) {
                    count++;
                }
            }
            if (count > 0) {
                LOGGER.info("[SeriaMMOItemsAE] Pre-processed and resolved placeholders in " + count + " MMOItems template(s)!");
            }
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "[SeriaMMOItemsAE] Error while pre-processing MMOItems templates: " + t.getMessage(), t);
        }
        return count;
    }
}
