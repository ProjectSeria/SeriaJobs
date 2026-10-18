package id.seria.bossevent.display;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.config.EventConfig;
import id.seria.core.gui.menu.BaseSeriaMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class MessageService {

    private final SeriaBossEventPlugin plugin;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public MessageService(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Parses MiniMessage string with SeriaCore semantic colors and placeholders.
     */
    public Component parse(String text, Map<String, String> replacements) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String replaced = text;
        if (replacements != null && !replacements.isEmpty()) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue() != null ? entry.getValue() : "";
                replaced = replaced.replace("{" + key + "}", val)
                                   .replace("<" + key + ">", val)
                                   .replace("%" + key + "%", val);
            }
        }

        try {
            // Apply SeriaCore semantic tags
            replaced = BaseSeriaMenu.formatSemanticColorTags(replaced);
        } catch (Throwable ignored) {}

        try {
            return MINI_MESSAGE.deserialize(replaced);
        } catch (Exception e) {
            return Component.text(replaced);
        }
    }

    public Component parse(String text) {
        return parse(text, null);
    }

    public String resolveMessageTemplate(EventConfig eventConfig, String path, String def) {
        if (eventConfig != null && eventConfig.isOverrideMessages()) {
            Object custom = eventConfig.getCustomMessages().get(path);
            if (custom != null) {
                return String.valueOf(custom);
            }
        }
        return plugin.getConfigManager().getMessage(path, def);
    }

    public void sendMessage(CommandSender sender, String message, Map<String, String> replacements) {
        if (sender == null || message == null || message.isBlank()) return;
        sender.sendMessage(parse(message, replacements));
    }

    public void sendMessageWithPrefix(CommandSender sender, String message, Map<String, String> replacements) {
        if (sender == null || message == null || message.isBlank()) return;
        String prefix = plugin.getConfigManager().getPrefix();
        sender.sendMessage(parse(prefix + message, replacements));
    }

    public void broadcast(Component component, String scope, EventConfig eventConfig) {
        if (component == null) return;
        if ("world".equalsIgnoreCase(scope) && eventConfig != null && !eventConfig.getBosses().isEmpty()) {
            String worldName = eventConfig.getBosses().get(0).getWorld();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().getName().equalsIgnoreCase(worldName)) {
                    player.sendMessage(component);
                }
            }
            Bukkit.getConsoleSender().sendMessage(component);
        } else {
            // Default "server"
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(component);
            }
            Bukkit.getConsoleSender().sendMessage(component);
        }
    }

    public void playSound(Player player, String soundName) {
        if (player == null || soundName == null || soundName.isBlank()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase().trim());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    public void showTitle(Player player, Component titleComp, Component subComp, int inTicks, int stayTicks, int outTicks) {
        if (player == null) return;
        Title title = Title.title(
                titleComp,
                subComp,
                Title.Times.times(Duration.ofMillis(inTicks * 50L), Duration.ofMillis(stayTicks * 50L), Duration.ofMillis(outTicks * 50L))
        );
        player.showTitle(title);
    }
}
