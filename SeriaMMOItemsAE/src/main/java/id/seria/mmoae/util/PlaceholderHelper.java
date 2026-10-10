package id.seria.mmoae.util;

import id.seria.core.gui.menu.BaseSeriaMenu;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for resolving SeriaCore semantic tags (<sc:...>, <p:...>),
 * PlaceholderAPI placeholders (%...%), and Adventure MiniMessage gradients
 * before MMOItems registers item templates and on active ItemStacks.
 */
public class PlaceholderHelper {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final Pattern PAPI_PATTERN = Pattern.compile("%([^%]+)%");
    private static final Pattern SC_TAG_PATTERN = Pattern.compile("(<(/)?(?:sc|p|placeholder|seriaprefix):([a-zA-Z0-9_.-]+)>)");

    /**
     * Checks if a string contains any unparsed Seria semantic tags or PAPI placeholders.
     */
    public static boolean hasPlaceholders(String text) {
        if (text == null || text.isEmpty()) return false;
        return text.contains("<sc:") || text.contains("</sc:")
                || text.contains("<p:") || text.contains("</p:")
                || text.contains("<placeholder:") || text.contains("</placeholder:")
                || text.contains("<seriaprefix:") || text.contains("</seriaprefix:")
                || text.contains("<gradient:") || text.contains("<rainbow")
                || text.contains("<title-color>") || text.contains("</title-color>")
                || text.contains("<lore-color>") || text.contains("</lore-color>")
                || (text.indexOf('%') != -1 && PAPI_PATTERN.matcher(text).find());
    }

    /**
     * Resolves SeriaCore semantic tags, PlaceholderAPI placeholders, and MiniMessage color strings.
     *
     * @param player Optional Player context (can be null for global/template resolution).
     * @param text   The raw input string.
     * @return The text with all tags and placeholders resolved to MiniMessage syntax.
     */
    public static String resolveString(@Nullable Player player, String text) {
        if (text == null || text.isEmpty()) return "";

        String result = text;

        // 1. Resolve SeriaCore semantic tags (<sc:...>, <p:...>, <title-color>, etc.)
        if (isSeriaCoreAvailable()) {
            try {
                result = BaseSeriaMenu.formatSemanticColorTags(result);
            } catch (Throwable ignored) {}
        } else if (result.contains("<sc:") || result.contains("<p:") || result.contains("<seriaprefix:")) {
            result = resolveSCTagsFallback(player, result);
        }

        // 2. Resolve PlaceholderAPI (%...%) if available
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && result.indexOf('%') != -1) {
            try {
                result = PlaceholderAPI.setPlaceholders(player, result);
            } catch (Throwable ignored) {}
        }

        return result;
    }

    /**
     * Resolves the string and converts it to a legacy Minecraft color string (§x§f§f...).
     * This is ideal for MMOItems template names and Bukkit ItemMeta#setDisplayName.
     */
    public static String parseToLegacy(@Nullable Player player, String text) {
        if (text == null || text.isEmpty()) return "";

        String resolved = resolveString(player, text);

        // If it contains MiniMessage tags, parse through MiniMessage to Legacy Section
        if (containsMiniMessageTags(resolved)) {
            try {
                Component comp = MINI_MESSAGE.deserialize(resolved)
                        .decoration(TextDecoration.ITALIC, false);
                return LEGACY_SERIALIZER.serialize(comp);
            } catch (Throwable ignored) {
                // In case of syntax error in MiniMessage tag, fallback to ChatColor translation
            }
        }

        return ChatColor.translateAlternateColorCodes('&', resolved);
    }

    /**
     * Resolves the string and parses it into an Adventure Component without default italics.
     */
    public static Component parseToComponent(@Nullable Player player, String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        String resolved = resolveString(player, text);

        if (containsMiniMessageTags(resolved)) {
            try {
                return MINI_MESSAGE.deserialize(resolved)
                        .decoration(TextDecoration.ITALIC, false);
            } catch (Throwable ignored) {}
        }

        String legacy = ChatColor.translateAlternateColorCodes('&', resolved);
        return LEGACY_SERIALIZER.deserialize(legacy).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Inspects an ItemStack's display name and lore. If unparsed tags are detected,
     * updates the ItemMeta with fully parsed Adventure Components and legacy strings.
     *
     * @param item   The item to inspect and fix.
     * @param player Optional Player context.
     * @return true if the item was modified.
     */
    public static boolean processItemMeta(ItemStack item, @Nullable Player player) {
        if (item == null || item.getType().isAir()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        boolean modified = false;

        // 1. Process Display Name
        if (meta.hasDisplayName()) {
            String rawName = getRawDisplayName(meta);
            if (hasPlaceholders(rawName)) {
                Component comp = parseToComponent(player, rawName);
                String legacy = parseToLegacy(player, rawName);
                try {
                    meta.displayName(comp);
                } catch (Throwable ignored) {}
                meta.setDisplayName(legacy);
                modified = true;
            }
        }

        // 2. Process Lore
        if (meta.hasLore()) {
            List<String> rawLore = getRawLore(meta);
            if (rawLore != null && !rawLore.isEmpty()) {
                boolean loreChanged = false;
                List<Component> compLore = new ArrayList<>(rawLore.size());
                List<String> legacyLore = new ArrayList<>(rawLore.size());

                for (String line : rawLore) {
                    if (hasPlaceholders(line)) {
                        compLore.add(parseToComponent(player, line));
                        legacyLore.add(parseToLegacy(player, line));
                        loreChanged = true;
                    } else {
                        compLore.add(LEGACY_SERIALIZER.deserialize(line).decoration(TextDecoration.ITALIC, false));
                        legacyLore.add(line);
                    }
                }

                if (loreChanged) {
                    try {
                        meta.lore(compLore);
                    } catch (Throwable ignored) {}
                    meta.setLore(legacyLore);
                    modified = true;
                }
            }
        }

        if (modified) {
            item.setItemMeta(meta);
        }

        return modified;
    }

    private static String getRawDisplayName(ItemMeta meta) {
        try {
            Component comp = meta.displayName();
            if (comp != null) {
                // If the component contains raw unparsed text
                String serialized = MINI_MESSAGE.serialize(comp);
                if (hasPlaceholders(serialized)) return serialized;
            }
        } catch (Throwable ignored) {}
        return meta.getDisplayName();
    }

    private static List<String> getRawLore(ItemMeta meta) {
        try {
            List<Component> comps = meta.lore();
            if (comps != null && !comps.isEmpty()) {
                List<String> list = new ArrayList<>();
                for (Component comp : comps) {
                    list.add(MINI_MESSAGE.serialize(comp));
                }
                return list;
            }
        } catch (Throwable ignored) {}
        return meta.getLore();
    }

    private static boolean containsMiniMessageTags(String text) {
        if (text == null) return false;
        return text.contains("<") && text.contains(">");
    }

    private static boolean isSeriaCoreAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("SeriaCore");
    }

    private static String resolveSCTagsFallback(@Nullable Player player, String text) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return text;
        }

        Matcher matcher = SC_TAG_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String tagKey = matcher.group(3).toLowerCase().trim();
            String papiTag = "%sc_" + tagKey + "_mini%";
            String resolved = PlaceholderAPI.setPlaceholders(player, papiTag);
            if (resolved.equals(papiTag)) {
                papiTag = "%seriaprefix_" + tagKey + "_mini%";
                resolved = PlaceholderAPI.setPlaceholders(player, papiTag);
            }
            if (resolved.equals(papiTag)) {
                resolved = "";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
