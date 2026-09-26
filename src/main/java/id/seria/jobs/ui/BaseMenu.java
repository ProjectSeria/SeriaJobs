package id.seria.jobs.ui;

import id.seria.core.gui.menu.MenuFunctionEngine;
import id.seria.core.gui.menu.MenuRequirementEvaluator;
import id.seria.jobs.SeriaJobs;
import id.seria.jobs.utils.TextUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public abstract class BaseMenu implements InventoryHolder {

    protected final SeriaJobs plugin;
    protected final Player player;
    protected final FileConfiguration menuConfig;
    protected Inventory inventory;

    protected final Map<Integer, Consumer<InventoryClickEvent>> clickActions = new HashMap<>();
    protected final Map<Integer, Consumer<InventoryClickEvent>> leftClickActions = new HashMap<>();
    protected final Map<Integer, Consumer<InventoryClickEvent>> rightClickActions = new HashMap<>();
    protected final Map<Integer, Consumer<InventoryClickEvent>> shiftLeftClickActions = new HashMap<>();
    protected final Map<Integer, Consumer<InventoryClickEvent>> shiftRightClickActions = new HashMap<>();
    protected final Map<Integer, Consumer<InventoryClickEvent>> middleClickActions = new HashMap<>();

    public BaseMenu(SeriaJobs plugin, Player player, String menuConfigName) {
        this.plugin = plugin;
        this.player = player;
        this.menuConfig = plugin.getMenuRegistry().getMenu(menuConfigName);
    }

    public abstract void build();

    public void open() {
        build();
        player.openInventory(inventory);
    }

    public void refresh() {
        if (inventory == null) {
            open();
            return;
        }
        clickActions.clear();
        leftClickActions.clear();
        rightClickActions.clear();
        shiftLeftClickActions.clear();
        shiftRightClickActions.clear();
        middleClickActions.clear();
        build();
        player.updateInventory();
    }

    public void refreshSlots(int... slots) {
        if (inventory == null) {
            open();
            return;
        }
        if (slots == null || slots.length == 0) {
            refresh();
            return;
        }
        for (int s : slots) {
            refreshSlot(s);
        }
    }

    public void refreshSlot(int slot) {
        if (inventory == null || slot < 0 || slot >= inventory.getSize())
            return;

        // 1. Delegate to custom slot refresh first if handled (e.g. OrderDepositMenu
        // cards)
        if (refreshCustomSlot(slot)) {
            player.updateInventory();
            return;
        }

        // 2. Check static items
        if (menuConfig != null && menuConfig.isConfigurationSection("items")) {
            ConfigurationSection itemsSec = menuConfig.getConfigurationSection("items");
            List<ConfigurationSection> candidates = new ArrayList<>();
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection sec = itemsSec.getConfigurationSection(key);
                if (sec == null)
                    continue;
                List<Integer> targetSlots = getSlots(sec);
                if (targetSlots.contains(slot)) {
                    candidates.add(sec);
                }
            }
            if (!candidates.isEmpty()) {
                candidates.sort((a, b) -> Integer.compare(b.getInt("priority", 0), a.getInt("priority", 0)));
                for (ConfigurationSection candidate : candidates) {
                    if (MenuRequirementEvaluator.checkViewRequirement(candidate, player)) {
                        ItemStack item = buildItem(candidate);
                        inventory.setItem(slot, item);
                        clickActions.remove(slot);
                        leftClickActions.remove(slot);
                        rightClickActions.remove(slot);
                        shiftLeftClickActions.remove(slot);
                        shiftRightClickActions.remove(slot);
                        middleClickActions.remove(slot);
                        bindActions(slot, candidate);
                        player.updateInventory();
                        return;
                    }
                }
            }
        }

        // 3. Check fillers
        if (menuConfig != null && menuConfig.isConfigurationSection("fillers")) {
            ConfigurationSection fillersSec = menuConfig.getConfigurationSection("fillers");
            for (String key : fillersSec.getKeys(false)) {
                ConfigurationSection sec = fillersSec.getConfigurationSection(key);
                if (sec == null)
                    continue;
                List<Integer> slots = getSlots(sec);
                if (slots.contains(slot)) {
                    if (MenuRequirementEvaluator.checkViewRequirement(sec, player)) {
                        ItemStack item = buildItem(sec);
                        inventory.setItem(slot, item.clone());
                        clickActions.put(slot, e -> e.setCancelled(true));
                        player.updateInventory();
                        return;
                    }
                }
            }
        }

        player.updateInventory();
    }

    protected boolean refreshCustomSlot(int slot) {
        return false;
    }

    protected String replacePlaceholders(String text) {
        if (text == null)
            return "";
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("SeriaCore")) {
                text = id.seria.core.placeholder.CustomPlaceholderManager.parse(player, text);
            }
        } catch (Throwable ignored) {}

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && text.contains("%")) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    protected void createInventory(int defaultSize, String defaultTitle) {
        int size = menuConfig != null ? menuConfig.getInt("size", defaultSize) : defaultSize;
        String rawTitle = menuConfig != null ? menuConfig.getString("title", defaultTitle) : defaultTitle;
        rawTitle = replacePlaceholders(rawTitle);
        Component titleComponent = TextUtil.parseForPlayer(player, rawTitle);

        if (this.inventory == null || this.inventory.getSize() != size) {
            this.inventory = Bukkit.createInventory(this, size, titleComponent);
        } else {
            this.inventory.clear();
        }
    }

    public ItemStack resolveBaseItem(ConfigurationSection sec, Player player) {
        if (sec == null)
            return new ItemStack(Material.STONE);

        // 1. MMOItems
        if (sec.contains("mmoitem") || sec.contains("mmoitem-id")) {
            if (plugin.getHookManager().isMMOItemsLoaded()) {
                String mmoType = sec.getString("mmoitem-type");
                String mmoId = sec.getString("mmoitem-id");
                if (sec.contains("mmoitem")) {
                    String val = sec.getString("mmoitem");
                    if (val != null && val.contains(":")) {
                        String[] parts = val.split(":", 2);
                        mmoType = parts[0];
                        mmoId = parts[1];
                    } else if (mmoId == null) {
                        mmoId = val;
                    }
                }
                if (mmoType != null && mmoId != null) {
                    ItemStack item = plugin.getHookManager().getMMOItemsHook().getItem(mmoType, mmoId);
                    if (item != null)
                        return item.clone();
                }
            }
        }

        // 2. MythicMobs Item
        if (sec.contains("mythicitem") || sec.contains("mythic-id")) {
            if (plugin.getHookManager().isMythicMobsLoaded()) {
                String itemId = sec.getString("mythicitem", sec.getString("mythic-id"));
                if (itemId != null) {
                    ItemStack item = plugin.getHookManager().getMythicMobsHook().generateItem(itemId, 1);
                    if (item != null)
                        return item.clone();
                }
            }
        }

        // 3. CustomFishing Item
        if (sec.contains("customfishing") || sec.contains("fishing-id")) {
            if (plugin.getHookManager().isCustomFishingLoaded()) {
                String itemId = sec.getString("customfishing", sec.getString("fishing-id"));
                if (itemId != null) {
                    ItemStack item = plugin.getHookManager().getCustomFishingHook().generateItem(itemId, 1);
                    if (item != null)
                        return item.clone();
                }
            }
        }

        // 4. SeriaMagicBundle Item
        if (sec.contains("magicbundle") || sec.contains("bundle-id")) {
            if (plugin.getHookManager().isSeriaMagicBundleLoaded()) {
                String itemId = sec.getString("magicbundle", sec.getString("bundle-id"));
                if (itemId != null) {
                    ItemStack item = plugin.getHookManager().getSeriaMagicBundleHook().generateItem(itemId, 1);
                    if (item != null)
                        return item.clone();
                }
            }
        }

        // 5. SeriaCrafting Item
        if (sec.contains("seriacrafting") || sec.contains("crafting-id")) {
            if (plugin.getHookManager().isSeriaCraftingLoaded()) {
                String itemId = sec.getString("seriacrafting", sec.getString("crafting-id"));
                if (itemId != null) {
                    ItemStack item = plugin.getHookManager().getSeriaCraftingHook().generateItem(itemId, 1);
                    if (item != null)
                        return item.clone();
                }
            }
        }

        // 6. Vanilla Material (with placeholder and function resolution)
        String rawMat = sec.getString("material", "STONE");
        rawMat = resolvePlaceholdersAndFunctions(rawMat);

        Material mat = Material.matchMaterial(rawMat.toUpperCase().trim());
        if (mat == null)
            mat = Material.STONE;
        return new ItemStack(mat);
    }

    protected void applyFillers() {
        if (menuConfig == null || !menuConfig.isConfigurationSection("fillers"))
            return;

        ConfigurationSection fillersSec = menuConfig.getConfigurationSection("fillers");
        for (String key : fillersSec.getKeys(false)) {
            ConfigurationSection sec = fillersSec.getConfigurationSection(key);
            if (sec == null)
                continue;

            // Check view-requirement
            if (!MenuRequirementEvaluator.checkViewRequirement(sec, player)) {
                continue;
            }

            ItemStack item = buildItem(sec);
            List<Integer> slots = getSlots(sec);
            for (int slot : slots) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, item.clone());
                    clickActions.put(slot, e -> e.setCancelled(true));
                }
            }
        }
    }

    protected void applyStaticItems() {
        if (menuConfig == null || !menuConfig.isConfigurationSection("items"))
            return;

        ConfigurationSection itemsSec = menuConfig.getConfigurationSection("items");

        // Group candidate items by target slot
        Map<Integer, List<ConfigurationSection>> slotCandidates = new HashMap<>();

        for (String key : itemsSec.getKeys(false)) {
            ConfigurationSection sec = itemsSec.getConfigurationSection(key);
            if (sec == null)
                continue;

            if (sec.contains("taint") || sec.contains("taints")) {
                continue;
            }

            List<Integer> slots = getSlots(sec);
            for (int slot : slots) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    slotCandidates.computeIfAbsent(slot, k -> new ArrayList<>()).add(sec);
                }
            }
        }

        // Sort candidates by priority descending and place the highest-priority item
        // passing view-requirement
        for (Map.Entry<Integer, List<ConfigurationSection>> entry : slotCandidates.entrySet()) {
            int slot = entry.getKey();
            List<ConfigurationSection> candidates = entry.getValue();

            candidates.sort((a, b) -> Integer.compare(b.getInt("priority", 0), a.getInt("priority", 0)));

            for (ConfigurationSection candidate : candidates) {
                if (MenuRequirementEvaluator.checkViewRequirement(candidate, player)) {
                    ItemStack item = buildItem(candidate);
                    inventory.setItem(slot, item);
                    bindActions(slot, candidate);
                    break;
                }
            }
        }
    }

    public ItemStack buildItem(ConfigurationSection sec) {
        ItemStack item = resolveBaseItem(sec, player);
        int amount = sec.getInt("amount", 1);
        if (amount > 1)
            item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Custom Model Data
            if (sec.contains("custom-model-data")) {
                meta.setCustomModelData(sec.getInt("custom-model-data"));
            }

            // Potion Color (Tipped Arrow / Potion)
            if (sec.contains("potion") && meta instanceof PotionMeta potionMeta) {
                String hex = sec.getString("potion", "#518EFF");
                Color color = parseHexColor(hex);
                if (color != null) {
                    potionMeta.setColor(color);
                }
            }

            // Player Head
            if (item.getType() == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta) {
                String skullOwner = sec.getString("skull-owner", "%player_name%");
                skullOwner = replacePlaceholders(skullOwner);
                skullOwner = skullOwner.replace("%player_name%", player.getName());
                skullMeta.setOwningPlayer(player);
            }

            // Item Flags
            if (sec.contains("item-flags")) {
                for (String flagStr : sec.getStringList("item-flags")) {
                    try {
                        meta.addItemFlags(ItemFlag.valueOf(flagStr.trim().toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            // Name
            if (sec.contains("name")) {
                String rawName = resolvePlaceholdersAndFunctions(sec.getString("name"));
                meta.displayName(TextUtil.parseForPlayer(player, rawName));
            }

            // Lore
            if (sec.contains("lore")) {
                List<String> rawLore = sec.getStringList("lore");
                List<Component> lore = new ArrayList<>();
                ConfigurationSection phSec = menuConfig != null ? menuConfig.getConfigurationSection("placeholders")
                        : null;

                for (String line : rawLore) {
                    // Check if line is custom placeholder returning a list (e.g.
                    // %placeholders_playerlevel-tier-get-status%)
                    if (phSec != null && line.trim().startsWith("%placeholders_") && line.trim().endsWith("%")) {
                        String phKey = line.trim().substring(1, line.trim().length() - 1);
                        Object res = MenuFunctionEngine.resolveCustomPlaceholder(phSec, phKey, player);
                        if (res instanceof List<?> list) {
                            for (Object o : list) {
                                lore.add(TextUtil.parseForPlayer(player, String.valueOf(o)));
                            }
                            continue;
                        } else if (res instanceof String str) {
                            lore.add(TextUtil.parseForPlayer(player, str));
                            continue;
                        }
                    }

                    String replaced = resolvePlaceholdersAndFunctions(line);
                    lore.add(TextUtil.parseForPlayer(player, replaced));
                }
                meta.lore(lore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private String resolvePlaceholdersAndFunctions(String text) {
        if (text == null || text.isEmpty())
            return "";

        if (menuConfig != null && menuConfig.isConfigurationSection("placeholders")
                && text.contains("%placeholders_")) {
            ConfigurationSection phSec = menuConfig.getConfigurationSection("placeholders");
            for (String key : phSec.getKeys(false)) {
                String token = "%placeholders_" + key + "%";
                if (text.contains(token)) {
                    Object res = MenuFunctionEngine.resolveCustomPlaceholder(phSec, key, player);
                    if (res instanceof String val) {
                        text = text.replace(token, val);
                    }
                }
            }
        }

        // 1. SeriaCore universal placeholder engine
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("SeriaCore")) {
                text = id.seria.core.placeholder.CustomPlaceholderManager.parse(player, text);
            }
        } catch (Throwable ignored) {}

        // 2. Direct custom placeholder resolution (%custom_...% and %seriajobs_custom_...%)
        if (plugin.getCustomPlaceholderManager() != null && (text.contains("%custom_") || text.contains("%seriajobs_custom_"))) {
            text = resolveCustomPlaceholdersDirectly(text);
        }

        // 3. Direct jobsorder placeholder resolution (%jobsorder_...)
        if (text.contains("%jobsorder_")) {
            text = resolveJobsOrderPlaceholdersDirectly(text);
        }

        text = MenuRequirementEvaluator.evaluateMathExpressions(text);
        text = replacePlaceholders(text);

        // 3. Fallback pass for nested custom placeholders after PAPI / Math
        if (plugin.getCustomPlaceholderManager() != null && (text.contains("%custom_") || text.contains("%seriajobs_custom_"))) {
            text = resolveCustomPlaceholdersDirectly(text);
        }
        if (text.contains("%jobsorder_")) {
            text = resolveJobsOrderPlaceholdersDirectly(text);
        }

        return text;
    }

    protected String resolveCustomPlaceholdersDirectly(String text) {
        if (text == null || text.isEmpty() || plugin.getCustomPlaceholderManager() == null) {
            return text;
        }
        var mgr = plugin.getCustomPlaceholderManager();
        int maxLoop = 5;
        while (maxLoop-- > 0 && (text.contains("%custom_") || text.contains("%seriajobs_custom_"))) {
            String prev = text;
            int start;
            while ((start = text.indexOf("%custom_")) != -1) {
                int end = text.indexOf('%', start + 8);
                if (end == -1) break;
                String token = text.substring(start, end + 1);
                String param = text.substring(start + 8, end);
                String resolved = mgr.evaluate(player, param);
                if (resolved != null) {
                    text = text.replace(token, resolved);
                } else {
                    break;
                }
            }
            while ((start = text.indexOf("%seriajobs_custom_")) != -1) {
                int end = text.indexOf('%', start + 18);
                if (end == -1) break;
                String token = text.substring(start, end + 1);
                String param = text.substring(start + 18, end);
                String resolved = mgr.evaluate(player, param);
                if (resolved != null) {
                    text = text.replace(token, resolved);
                } else {
                    break;
                }
            }
            if (text.equals(prev)) break;
        }
        return text;
    }

    protected String resolveJobsOrderPlaceholdersDirectly(String text) {
        if (text == null || text.isEmpty()) return text;
        id.seria.jobs.hook.JobsPlaceholderExpansion expansion = new id.seria.jobs.hook.JobsPlaceholderExpansion(plugin);
        int start;
        while ((start = text.indexOf("%jobsorder_")) != -1) {
            int end = text.indexOf('%', start + 11);
            if (end == -1) break;
            String token = text.substring(start, end + 1);
            String param = text.substring(start + 11, end);
            String resolved = expansion.onRequest(player, param);
            if (resolved != null) {
                text = text.replace(token, resolved);
            } else {
                break;
            }
        }
        return text;
    }

    private List<Integer> getSlots(ConfigurationSection sec) {
        List<Integer> slots = new ArrayList<>();
        if (sec.isList("slots")) {
            slots.addAll(sec.getIntegerList("slots"));
        } else if (sec.contains("slot")) {
            slots.add(sec.getInt("slot"));
        }
        return slots;
    }

    protected void bindActions(int slot, ConfigurationSection sec) {
        List<String> rawGeneral = sec.getStringList("click-actions");
        final List<String> generalActions = rawGeneral.isEmpty() ? sec.getStringList("actions") : rawGeneral;

        List<String> rawLeft = sec.getStringList("left-click-actions");
        final List<String> leftActions = rawLeft.isEmpty() ? sec.getStringList("left-click") : rawLeft;

        List<String> rawRight = sec.getStringList("right-click-actions");
        final List<String> rightActions = rawRight.isEmpty() ? sec.getStringList("right-click") : rawRight;

        List<String> rawShiftLeft = sec.getStringList("shift-left-click-actions");
        final List<String> shiftLeftActions = rawShiftLeft.isEmpty() ? sec.getStringList("shift-left-click")
                : rawShiftLeft;

        List<String> rawShiftRight = sec.getStringList("shift-right-click-actions");
        final List<String> shiftRightActions = rawShiftRight.isEmpty() ? sec.getStringList("shift-right-click")
                : rawShiftRight;

        List<String> rawMiddle = sec.getStringList("middle-click-actions");
        final List<String> middleActions = rawMiddle.isEmpty() ? sec.getStringList("middle-click") : rawMiddle;

        if (!generalActions.isEmpty()) {
            clickActions.put(slot, e -> {
                e.setCancelled(true);
                handleActions(generalActions);
            });
        }
        if (!leftActions.isEmpty()) {
            leftClickActions.put(slot, e -> {
                e.setCancelled(true);
                handleActions(leftActions);
            });
        }
        if (!rightActions.isEmpty()) {
            rightClickActions.put(slot, e -> {
                e.setCancelled(true);
                handleActions(rightActions);
            });
        }
        if (!shiftLeftActions.isEmpty()) {
            shiftLeftClickActions.put(slot, e -> {
                e.setCancelled(true);
                handleActions(shiftLeftActions);
            });
        }
        if (!shiftRightActions.isEmpty()) {
            shiftRightClickActions.put(slot, e -> {
                e.setCancelled(true);
                handleActions(shiftRightActions);
            });
        }
        if (!middleActions.isEmpty()) {
            middleClickActions.put(slot, e -> {
                e.setCancelled(true);
                handleActions(middleActions);
            });
        }
    }

    protected void handleActions(List<String> actions) {
        if (actions == null || actions.isEmpty())
            return;
        for (String act : actions) {
            if (act == null || act.isBlank()) continue;
            String trimmed = act.trim();

            // 1. Resolve custom placeholders and PAPI in action if present
            if (trimmed.contains("%") || trimmed.contains("{")) {
                trimmed = resolvePlaceholdersAndFunctions(trimmed);
                if (player != null) {
                    trimmed = trimmed.replace("%player_name%", player.getName())
                            .replace("{player_name}", player.getName())
                            .replace("%player%", player.getName())
                            .replace("{player}", player.getName());
                }
                trimmed = trimmed.trim();
            }

            if (trimmed.isEmpty()) continue;

            if (trimmed.equalsIgnoreCase("[CLOSE]")) {
                player.closeInventory();
            } else if (trimmed.toLowerCase().startsWith("[open_menu]")
                    || trimmed.toLowerCase().startsWith("[open-menu]")
                    || trimmed.toLowerCase().startsWith("[open]")) {
                int closing = trimmed.indexOf(']');
                String menuTarget = closing != -1 && closing + 1 < trimmed.length()
                        ? trimmed.substring(closing + 1).trim()
                        : "";
                menuTarget = resolvePlaceholdersAndFunctions(menuTarget);
                if (player != null) {
                    menuTarget = menuTarget.replace("%player_name%", player.getName())
                            .replace("{player_name}", player.getName())
                            .replace("%player%", player.getName())
                            .replace("{player}", player.getName());
                }
                openMenuByName(menuTarget);
            } else if (trimmed.toLowerCase().startsWith("[refresh")) {
                handleRefreshAction(trimmed);
            } else if (trimmed.toLowerCase().startsWith("[player]")) {
                String cmd = trimmed.substring("[player]".length()).trim();
                while (cmd.toLowerCase().startsWith("[player]")) {
                    cmd = cmd.substring("[player]".length()).trim();
                }
                cmd = resolvePlaceholdersAndFunctions(cmd);
                if (player != null) {
                    cmd = cmd.replace("%player_name%", player.getName())
                            .replace("{player_name}", player.getName())
                            .replace("%player%", player.getName())
                            .replace("{player}", player.getName());
                }
                if (!cmd.isEmpty()) {
                    player.performCommand(cmd);
                }
            } else if (trimmed.toLowerCase().startsWith("[console]")) {
                String cmd = trimmed.substring("[console]".length()).trim();
                while (cmd.toLowerCase().startsWith("[console]")) {
                    cmd = cmd.substring("[console]".length()).trim();
                }
                cmd = resolvePlaceholdersAndFunctions(cmd);
                if (player != null) {
                    cmd = cmd.replace("%player_name%", player.getName())
                            .replace("{player_name}", player.getName())
                            .replace("%player%", player.getName())
                            .replace("{player}", player.getName());
                }
                if (!cmd.isEmpty()) {
                    if (cmd.startsWith("%") && cmd.endsWith("%")) {
                        plugin.getLogger().warning("Skipping execution of unresolved placeholder command: " + cmd);
                        continue;
                    }
                    // Check if command is "serium take <player> <amount>" -> translate to excellent economy or hook
                    if (cmd.toLowerCase().startsWith("serium take ")) {
                        String[] parts = cmd.split("\\s+");
                        int amount = parts.length > 3 ? (int) Double.parseDouble(parts[3]) : 1;
                        if (!plugin.getJobsManager().deductSerium(player, amount)) {
                            String altCmd = "excelenteconomy withdraw " + player.getName() + " serium " + amount;
                            plugin.getLogger().info("Intercepting serium command for " + player.getName() + ": /" + altCmd);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), altCmd);
                        } else {
                            plugin.getLogger().info("Successfully deducted " + amount + " Serium via hook for " + player.getName());
                        }
                    } else {
                        plugin.getLogger().info("Executing [console] action for " + player.getName() + ": /" + cmd);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }
                }
            } else if (trimmed.toUpperCase().startsWith("[PLAYER_COMMAND]")) {
                String cmd = trimmed.substring("[PLAYER_COMMAND]".length()).trim();
                if (player != null) {
                    cmd = cmd.replace("%player_name%", player.getName())
                            .replace("{player_name}", player.getName())
                            .replace("%player%", player.getName())
                            .replace("{player}", player.getName());
                }
                if (!cmd.isEmpty()) {
                    player.performCommand(cmd);
                }
            } else if (trimmed.toUpperCase().startsWith("[CONSOLE_COMMAND]")) {
                String cmd = trimmed.substring("[CONSOLE_COMMAND]".length()).trim();
                if (player != null) {
                    cmd = cmd.replace("%player_name%", player.getName())
                            .replace("{player_name}", player.getName())
                            .replace("%player%", player.getName())
                            .replace("{player}", player.getName());
                }
                if (!cmd.isEmpty()) {
                    plugin.getLogger().info("Executing [CONSOLE_COMMAND] action: /" + cmd);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            } else if (trimmed.toLowerCase().startsWith("[sound]")) {
                String soundStr = trimmed.substring("[sound]".length()).trim();
                playSound(soundStr);
            } else if (trimmed.toLowerCase().startsWith("[deduct_gins]") || trimmed.toLowerCase().startsWith("[deduct-gins]")) {
                int closing = trimmed.indexOf(']');
                String param = closing != -1 && closing + 1 < trimmed.length() ? trimmed.substring(closing + 1).trim() : "";
                id.seria.jobs.model.JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
                int amount = !param.isEmpty() ? (int) Double.parseDouble(param) : plugin.getOrderGenerationService().calculateRerollCost(profile.getRerollCount());
                if (plugin.getJobsManager().getEconomyService() != null) {
                    plugin.getJobsManager().getEconomyService().withdraw(player, amount);
                    plugin.getLogger().info("Deducted " + amount + " Gins directly via Vault for " + player.getName());
                }
            } else if (trimmed.toLowerCase().startsWith("[deduct_serium]") || trimmed.toLowerCase().startsWith("[deduct-serium]")) {
                int closing = trimmed.indexOf(']');
                String param = closing != -1 && closing + 1 < trimmed.length() ? trimmed.substring(closing + 1).trim() : "";
                int amount = !param.isEmpty() ? (int) Double.parseDouble(param) : plugin.getConfig().getInt("reroll.serium-cost", 1);
                plugin.getJobsManager().deductSerium(player, amount);
                plugin.getLogger().info("Deducted " + amount + " Serium directly for " + player.getName());
            }
        }
    }

    private void playSound(String soundStr) {
        if (soundStr == null || soundStr.isBlank()) return;
        try {
            String[] parts = soundStr.split("\\s+");
            String soundName = parts[0].replace("minecraft:", "").toUpperCase();
            float vol = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, vol, pitch);
        } catch (Throwable t) {
            try {
                String keyStr = soundStr.split("\\s+")[0].toLowerCase();
                if (!keyStr.contains(":")) keyStr = "minecraft:" + keyStr;
                player.playSound(net.kyori.adventure.sound.Sound.sound(
                        net.kyori.adventure.key.Key.key(keyStr),
                        net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f));
            } catch (Throwable ignored) {}
        }
    }

    private void handleRefreshAction(String actionStr) {
        long delay = 1L;
        if (actionStr.contains("delay=")) {
            try {
                int start = actionStr.indexOf("delay=") + 6;
                int end = actionStr.indexOf(']', start);
                if (end != -1) {
                    double delaySec = Double.parseDouble(actionStr.substring(start, end).trim());
                    delay = Math.max(1L, (long) (delaySec * 20L));
                }
            } catch (Exception ignored) {
            }
        }

        int closingBracket = actionStr.indexOf(']');
        String param = closingBracket != -1 && closingBracket + 1 < actionStr.length()
                ? actionStr.substring(closingBracket + 1).trim()
                : "";

        final long finalDelay = delay;
        if (param.isEmpty() || param.equalsIgnoreCase("all")) {
            Bukkit.getScheduler().runTaskLater(plugin, this::refresh, finalDelay);
        } else {
            String[] tokens = param.split("[,\\s]+");
            List<Integer> slots = new ArrayList<>();
            for (String tok : tokens) {
                if (tok.equalsIgnoreCase("order-card") || tok.equalsIgnoreCase("orders")
                        || tok.equalsIgnoreCase("order_card") || tok.equalsIgnoreCase("cards")) {
                    if (this instanceof OrderDepositMenu odm) {
                        slots.addAll(odm.getOrderSlots());
                        continue;
                    }
                }
                try {
                    slots.add(Integer.parseInt(tok.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
            if (slots.isEmpty()) {
                Bukkit.getScheduler().runTaskLater(plugin, this::refresh, finalDelay);
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (int s : slots) {
                        refreshSlot(s);
                    }
                }, finalDelay);
            }
        }
    }

    public void handleClick(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        if (rawSlot < inventory.getSize()) {
            event.setCancelled(true);

            if (event.isShiftClick() && event.isRightClick() && shiftRightClickActions.containsKey(rawSlot)) {
                shiftRightClickActions.get(rawSlot).accept(event);
                return;
            }
            if (event.isShiftClick() && event.isLeftClick() && shiftLeftClickActions.containsKey(rawSlot)) {
                shiftLeftClickActions.get(rawSlot).accept(event);
                return;
            }
            if (event.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE
                    && middleClickActions.containsKey(rawSlot)) {
                middleClickActions.get(rawSlot).accept(event);
                return;
            }
            if (event.isLeftClick() && leftClickActions.containsKey(rawSlot)) {
                leftClickActions.get(rawSlot).accept(event);
                return;
            }
            if (event.isRightClick() && rightClickActions.containsKey(rawSlot)) {
                rightClickActions.get(rawSlot).accept(event);
                return;
            }
            if (clickActions.containsKey(rawSlot)) {
                clickActions.get(rawSlot).accept(event);
            }
        } else {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
        }
    }

    private Color parseHexColor(String hex) {
        if (hex == null)
            return null;
        String clean = hex.replace("#", "").trim();
        try {
            int rgb = Integer.parseInt(clean, 16);
            return Color.fromRGB(rgb);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void openMenuByName(String menuTarget) {
        if (menuTarget == null || menuTarget.isBlank()) return;
        menuTarget = menuTarget.trim();

        String[] tokens = menuTarget.split("\\s+");
        String rawTarget = tokens[0].trim();
        String param = tokens.length > 1 ? tokens[1].trim() : null;

        String clean = rawTarget.toLowerCase();
        if (clean.endsWith(".yml")) {
            clean = clean.substring(0, clean.length() - 4);
        } else if (clean.endsWith(".yaml")) {
            clean = clean.substring(0, clean.length() - 5);
        }

        // 1. Main Jobs Menu aliases
        if (clean.equals("main_job_menu") || clean.equals("main_jobs_menu")
                || clean.equals("main_job") || clean.equals("main_jobs")
                || clean.equals("main") || clean.equals("jobs")
                || clean.equals("jobs_menu") || clean.equals("jobs-menu")
                || clean.equals("bursa")) {
            new MainJobsMenu(plugin, player).open();
            return;
        }

        // 2. Orders GUI aliases
        if (clean.equals("job_order_gui") || clean.equals("job_orders_gui")
                || clean.equals("job_order") || clean.equals("job_orders")
                || clean.equals("order") || clean.equals("orders")
                || clean.equals("order_deposit_menu") || clean.equals("orders_gui")) {
            id.seria.jobs.model.Job targetJob = resolveJob(param);
            if (targetJob != null) {
                new OrderDepositMenu(plugin, player, targetJob).open();
            } else {
                new MainJobsMenu(plugin, player).open();
            }
            return;
        }

        // 3. Progression GUI aliases
        if (clean.equals("job_progression_gui") || clean.equals("job_progression")
                || clean.equals("progression") || clean.equals("roadmap")
                || clean.equals("rewards_roadmap")) {
            id.seria.jobs.model.Job targetJob = resolveJob(param);
            if (targetJob != null) {
                new JobProgressionMenu(plugin, player, targetJob).open();
            } else {
                new MainJobsMenu(plugin, player).open();
            }
            return;
        }

        // 4. Registry-based GUI (GenericMenu fallback for any registered custom YAML)
        if (plugin.getMenuRegistry() != null) {
            String ymlName = clean + ".yml";
            if (plugin.getMenuRegistry().getMenu(ymlName) != null) {
                new GenericMenu(plugin, player, ymlName).open();
                return;
            }
            if (plugin.getMenuRegistry().getMenu(rawTarget) != null) {
                new GenericMenu(plugin, player, rawTarget).open();
                return;
            }
        }

        plugin.getLogger().warning("Could not find menu to open for target: '" + menuTarget + "'");
    }

    protected id.seria.jobs.model.Job resolveJob(String param) {
        if (param != null && !param.isBlank()) {
            id.seria.jobs.model.Job j = plugin.getJobRegistry().getJob(param.trim());
            if (j != null) return j;
        }
        if (this instanceof OrderDepositMenu orderMenu) {
            return orderMenu.getJob();
        }
        if (this instanceof JobProgressionMenu progMenu) {
            return progMenu.getJob();
        }
        if (player != null && plugin.getPlayerDataManager() != null) {
            id.seria.jobs.model.JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
            if (profile != null && !profile.getActiveJobs().isEmpty()) {
                String jobId = profile.getActiveJobs().iterator().next();
                id.seria.jobs.model.Job j = plugin.getJobRegistry().getJob(jobId);
                if (j != null) return j;
            }
        }
        if (plugin.getJobRegistry() != null && !plugin.getJobRegistry().getJobs().isEmpty()) {
            return plugin.getJobRegistry().getJobs().iterator().next();
        }
        return null;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
