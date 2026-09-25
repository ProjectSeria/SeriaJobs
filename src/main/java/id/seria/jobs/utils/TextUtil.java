package id.seria.jobs.utils;

import id.seria.jobs.SeriaJobs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static Component parse(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }
        String prepared = prepareLegacyAndHex(formatSemanticColors(text));
        Component component;
        try {
            // Enforce <!italic> base context unless explicitly starting with italic tag
            String mmText = prepared.startsWith("<!italic>") || prepared.startsWith("<!i>")
                    ? prepared
                    : "<!italic>" + prepared;
            component = MINI_MESSAGE.deserialize(mmText);
        } catch (Exception e) {
            component = LEGACY_SERIALIZER.deserialize(prepared.replace('&', '§'));
        }
        component = component.applyFallbackStyle(net.kyori.adventure.text.format.Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build());
        return removeDefaultItalic(component);
    }

    /**
     * Ubah tag warna semantik Seria ({@code <title-color>}, {@code <lore-color>}, {@code <money-color>}, ...)
     * menjadi tag MiniMessage biasa memakai palet SeriaCore. Tanpa ini, tag tersebut tampil mentah di GUI
     * karena MiniMessage tidak mengenalnya.
     *
     * <p>SeriaCore hanya softdepend, jadi kelasnya dipanggil setelah dicek aktif.
     */
    private static String formatSemanticColors(String text) {
        if (!text.contains("-color>") && !text.contains("<action-icon>")) return text;
        if (!org.bukkit.Bukkit.getPluginManager().isPluginEnabled("SeriaCore")) return text;
        try {
            return id.seria.core.gui.menu.BaseSeriaMenu.formatSemanticColorTags(text);
        } catch (LinkageError ignored) {
            return text; // SeriaCore versi lama tanpa formatter
        }
    }

    /**
     * Recursively walks the Component tree and sets TextDecoration.ITALIC to FALSE
     * for any component where italic is NOT_SET (null).
     * Explicit italic decorations (TRUE) are strictly preserved.
     */
    public static Component removeDefaultItalic(@NotNull Component component) {
        Component result = component;
        if (result.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            result = result.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }
        if (!result.children().isEmpty()) {
            List<Component> newChildren = new ArrayList<>(result.children().size());
            for (Component child : result.children()) {
                newChildren.add(removeDefaultItalic(child));
            }
            result = result.children(newChildren);
        }
        return result;
    }

    public static Component parseForPlayer(@Nullable Player player, @Nullable String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }

        if (player != null) {
            text = replaceLuckPermsPlaceholders(player, text);
        }

        // Parse PAPI first if player is available
        if (player != null && SeriaJobs.getInstance().getHookManager().isPapiLoaded()) {
            try {
                text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            } catch (Throwable ignored) {}
        }

        // Apply SeriaTranslator translation if hooked
        if (player != null && SeriaJobs.getInstance().getHookManager().isSeriaTranslatorLoaded()) {
            try {
                text = SeriaJobs.getInstance().getHookManager().getSeriaTranslatorHook().translate(player, text);
            } catch (Throwable ignored) {}
        }

        return parse(text);
    }

    public static String replaceLuckPermsPlaceholders(@NotNull Player player, @NotNull String text) {
        if (text.isEmpty()) return text;
        text = text.replace("{player}", player.getName()).replace("%player%", player.getName());

        var hookMgr = SeriaJobs.getInstance().getHookManager();
        var lp = hookMgr != null ? hookMgr.getLuckPermsHook() : null;

        if (text.contains("{luckperms_group}") || text.contains("{lp_group}")) {
            String group = lp != null && lp.isLoaded() ? lp.getPrimaryGroup(player) : "default";
            text = text.replace("{luckperms_group}", group).replace("{lp_group}", group);
        }
        if (text.contains("{luckperms_prefix}") || text.contains("{lp_prefix}")) {
            String prefix = lp != null && lp.isLoaded() ? lp.getPrefix(player) : "";
            text = text.replace("{luckperms_prefix}", prefix != null ? prefix : "").replace("{lp_prefix}", prefix != null ? prefix : "");
        }
        if (text.contains("{extra_items}") || text.contains("{luckperms_extra_items}")) {
            int extra = lp != null ? lp.getExtraJobItems(player, null) : 0;
            text = text.replace("{extra_items}", String.valueOf(extra)).replace("{luckperms_extra_items}", String.valueOf(extra));
        }

        // Pattern matching for {luckperms_meta:<key>} and {lp_meta:<key>}
        if (text.contains("{luckperms_meta:") || text.contains("{lp_meta:")) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{(?:luckperms_meta|lp_meta):([a-zA-Z0-9._-]+)\\}");
            java.util.regex.Matcher matcher = pattern.matcher(text);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String metaKey = matcher.group(1);
                String val = lp != null && lp.isLoaded() ? lp.getMeta(player, metaKey) : null;
                matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(val != null ? val : "0"));
            }
            matcher.appendTail(sb);
            text = sb.toString();
        }

        return text;
    }

    public static List<Component> parseListForPlayer(@Nullable Player player, @Nullable List<String> lines) {
        List<Component> list = new ArrayList<>();
        if (lines == null) return list;
        for (String line : lines) {
            list.add(parseForPlayer(player, line));
        }
        return list;
    }

    public static String toLegacy(@NotNull Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }

    private static String prepareLegacyAndHex(String text) {
        if (text == null) return "";

        // Convert &#RRGGBB or §#RRGGBB to <#RRGGBB>
        Matcher hexMatcher = Pattern.compile("[&§]#([A-Fa-f0-9]{6})").matcher(text);
        StringBuffer sb = new StringBuffer();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(sb, "<#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(sb);
        String hexConverted = sb.toString();

        // Convert Minecraft Bukkit legacy hex format &x&r&r&g&g&b&b or §x§r§r§g§g§b§b
        Matcher bukkitHexMatcher = Pattern.compile("[&§]x([&§][A-Fa-f0-9]){6}").matcher(hexConverted);
        StringBuffer sbBukkit = new StringBuffer();
        while (bukkitHexMatcher.find()) {
            String found = bukkitHexMatcher.group();
            String cleanHex = found.replaceAll("[&§]x?", "");
            bukkitHexMatcher.appendReplacement(sbBukkit, "<#" + cleanHex + ">");
        }
        bukkitHexMatcher.appendTail(sbBukkit);
        String result = sbBukkit.toString();

        // Convert standard & and § color and formatting codes to MiniMessage tags
        return result
                .replace("&0", "<black>").replace("§0", "<black>")
                .replace("&1", "<dark_blue>").replace("§1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("§2", "<dark_green>")
                .replace("&3", "<dark_aqua>").replace("§3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("§4", "<dark_red>")
                .replace("&5", "<dark_purple>").replace("§5", "<dark_purple>")
                .replace("&6", "<gold>").replace("§6", "<gold>")
                .replace("&7", "<gray>").replace("§7", "<gray>")
                .replace("&8", "<dark_gray>").replace("§8", "<dark_gray>")
                .replace("&9", "<blue>").replace("§9", "<blue>")
                .replace("&a", "<green>").replace("§a", "<green>")
                .replace("&b", "<aqua>").replace("§b", "<aqua>")
                .replace("&c", "<red>").replace("§c", "<red>")
                .replace("&d", "<light_purple>").replace("§d", "<light_purple>")
                .replace("&e", "<yellow>").replace("§e", "<yellow>")
                .replace("&f", "<white>").replace("§f", "<white>")
                .replace("&l", "<bold>").replace("§l", "<bold>")
                .replace("&m", "<strikethrough>").replace("§m", "<strikethrough>")
                .replace("&n", "<underlined>").replace("§n", "<underlined>")
                .replace("&o", "<italic>").replace("§o", "<italic>")
                .replace("&r", "<reset>").replace("§r", "<reset>");
    }

    public static MiniMessage getMiniMessage() {
        return MINI_MESSAGE;
    }
}
