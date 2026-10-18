package id.seria.counter.utils;

import id.seria.core.gui.menu.BaseSeriaMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private TextUtil() {}

    /**
     * Resolves SeriaCore semantic color tags and parses string into Adventure Component.
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        String formatted = BaseSeriaMenu.formatSemanticColorTags(text);
        return MINI_MESSAGE.deserialize(formatted);
    }

    /**
     * Resolves semantic tags, replaces placeholder tokens, and parses into Component.
     */
    public static Component parse(String text, Map<String, String> replacements) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        String replaced = text;
        if (replacements != null && !replacements.isEmpty()) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                replaced = replaced.replace("{" + entry.getKey() + "}", entry.getValue())
                                   .replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return parse(replaced);
    }

    /**
     * Sends formatted message to CommandSender with prefix.
     */
    public static void sendMessage(CommandSender sender, String prefix, String message, Map<String, String> replacements) {
        if (sender == null || message == null || message.isBlank()) return;
        String combined = (prefix != null ? prefix : "") + message;
        sender.sendMessage(parse(combined, replacements));
    }

    /**
     * Sends a list of lines with placeholders replaced.
     */
    public static void sendList(CommandSender sender, List<String> lines, Map<String, String> replacements) {
        if (sender == null || lines == null || lines.isEmpty()) return;
        for (String line : lines) {
            sender.sendMessage(parse(line, replacements));
        }
    }
}
