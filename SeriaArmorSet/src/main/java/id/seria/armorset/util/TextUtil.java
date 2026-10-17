package id.seria.armorset.util;

import id.seria.armorset.SeriaArmorSetPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String resolve(@Nullable Player player, @NotNull String text) {
        if (text.isEmpty()) return "";

        String result = text;

        if (SeriaArmorSetPlugin.getInstance().getSeriaCoreHook().isAvailable()) {
            try {
                result = id.seria.core.gui.menu.BaseSeriaMenu.formatSemanticColorTags(result);
            } catch (Throwable ignored) {}
        } else if (result.contains("<sc:") || result.contains("<p:") || result.contains("<seriaprefix:")) {
            result = resolveSCTagsFallback(result);
        }

        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                result = PlaceholderAPI.setPlaceholders(player, result);
            } catch (Throwable ignored) {}
        }

        return result;
    }

    public static Component toComponent(@Nullable Player player, @NotNull String text) {
        String resolved = resolve(player, text);

        if (resolved.contains("&") || resolved.contains("§")) {
            resolved = translateLegacyToMiniMessage(resolved);
        }

        try {
            return MINI_MESSAGE.deserialize(resolved);
        } catch (Throwable t) {
            return LEGACY_SERIALIZER.deserialize(ChatColor.translateAlternateColorCodes('&', resolved));
        }
    }

    public static String translateLegacyToMiniMessage(@NotNull String input) {
        String text = input;

        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(text);
        StringBuffer hexBuffer = new StringBuffer();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(hexBuffer, "<#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(hexBuffer);
        text = hexBuffer.toString();

        text = text.replace("&0", "<black>").replace("§0", "<black>")
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
                .replace("&k", "<obfuscated>").replace("§k", "<obfuscated>")
                .replace("&l", "<bold>").replace("§l", "<bold>")
                .replace("&m", "<strikethrough>").replace("§m", "<strikethrough>")
                .replace("&n", "<underlined>").replace("§n", "<underlined>")
                .replace("&o", "<italic>").replace("§o", "<italic>")
                .replace("&r", "<reset>").replace("§r", "<reset>");

        return text;
    }

    private static String resolveSCTagsFallback(String text) {
        return text.replace("<sc:prefix>", "<gradient:#FF8A00:#E52E71><bold>SeriaSet</bold></gradient> <dark_gray>»</dark_gray> ")
                .replace("<sc:success-color>", "<green>")
                .replace("</sc:success-color>", "</green>")
                .replace("<sc:error-color>", "<red>")
                .replace("</sc:error-color>", "</red>")
                .replace("<sc:info-color>", "<yellow>")
                .replace("</sc:info-color>", "</yellow>")
                .replace("<title-color>", "<gold><bold>")
                .replace("</title-color>", "</bold></gold>")
                .replace("<lore-color>", "<gray>")
                .replace("</lore-color>", "</gray>");
    }

    public static void sendPrefixMessage(@NotNull CommandSender sender, @NotNull String message) {
        String prefix = SeriaArmorSetPlugin.getInstance().getConfiguration().getMessagePrefix();
        sendMessage(sender, prefix + message);
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        Player player = sender instanceof Player ? (Player) sender : null;
        Component component = toComponent(player, message);
        sender.sendMessage(component);
    }
}