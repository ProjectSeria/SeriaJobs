package id.seria.counter.service;

import id.seria.counter.SeriaCounterPlugin;
import id.seria.counter.model.Counter;
import id.seria.counter.model.CounterStep;
import id.seria.counter.utils.TextUtil;
import id.seria.core.gui.menu.BaseSeriaMenu;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CounterActionService {

    private final SeriaCounterPlugin plugin;

    public CounterActionService(SeriaCounterPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes a list of action strings in sequential order on the server main thread.
     */
    public void executeActions(Counter counter, CounterStep step, List<String> actions, CommandSender source) {
        if (actions == null || actions.isEmpty()) return;

        Runnable task = () -> {
            for (String rawAction : actions) {
                if (rawAction == null || rawAction.isBlank()) continue;
                try {
                    executeSingleAction(counter, step, rawAction, source);
                } catch (Throwable t) {
                    plugin.getLogger().warning("[SeriaCounter] Error executing action '" + rawAction + "' on counter " + counter.getId() + ": " + t.getMessage());
                    if (plugin.getConfigManager().isDebug()) {
                        t.printStackTrace();
                    }
                }
            }
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    private void executeSingleAction(Counter counter, CounterStep step, String rawAction, CommandSender source) {
        // 1. Replace counter token placeholders
        String action = rawAction
                .replace("%counter_id%", counter.getId())
                .replace("%counter_name%", counter.getDisplayName())
                .replace("%counter_value%", String.valueOf(counter.getCurrentValue()))
                .replace("%counter_goal%", String.valueOf(step.getGoal()))
                .replace("%counter_step%", String.valueOf(step.getStepNumber()))
                .replace("%counter_step_name%", step.getName())
                .replace("%counter_total_steps%", String.valueOf(counter.getTotalSteps()))
                .replace("%counter_max_steps%", String.valueOf(counter.getTotalSteps()))
                .replace("%counter_progress%", String.format("%.1f", counter.getProgress()));

        if (source != null) {
            action = action.replace("%player%", source.getName())
                           .replace("{player}", source.getName());
        }

        // 2. Format Seria semantic tags (<sc:...>)
        action = BaseSeriaMenu.formatSemanticColorTags(action).trim();

        // 3. Dispatch based on prefix
        String lower = action.toLowerCase();
        if (lower.startsWith("[console]")) {
            String cmd = action.substring("[console]".length()).trim();
            if (!cmd.isEmpty()) {
                if (plugin.getConfigManager().isDebug()) {
                    plugin.getLogger().info("[SeriaCounter] Executing console action: " + cmd);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        } else if (lower.startsWith("[player]")) {
            String cmd = action.substring("[player]".length()).trim();
            if (!cmd.isEmpty()) {
                if (source instanceof Player player) {
                    player.performCommand(cmd);
                } else {
                    // Fallback to console if triggered by console/system
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        } else if (lower.startsWith("[broadcast]")) {
            String msg = action.substring("[broadcast]".length()).trim();
            if (!msg.isEmpty()) {
                Bukkit.broadcast(TextUtil.parse(msg));
            }
        } else if (lower.startsWith("[message]")) {
            String msg = action.substring("[message]".length()).trim();
            if (!msg.isEmpty() && source != null) {
                source.sendMessage(TextUtil.parse(msg));
            }
        } else if (lower.startsWith("[sound]")) {
            String soundSpec = action.substring("[sound]".length()).trim();
            playSound(soundSpec);
        } else {
            // Default fallback: if command starts with /, strip it and run console
            String cmd = action.startsWith("/") ? action.substring(1).trim() : action;
            if (!cmd.isEmpty()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }

    private void playSound(String soundSpec) {
        if (soundSpec == null || soundSpec.isBlank()) return;
        try {
            String[] parts = soundSpec.split("\\s+");
            String soundName = parts[0].replace("minecraft:", "").toUpperCase();
            float vol = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            Sound sound = Sound.valueOf(soundName);
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), sound, vol, pitch);
            }
        } catch (IllegalArgumentException e) {
            try {
                String keyStr = soundSpec.split("\\s+")[0].toLowerCase();
                if (!keyStr.contains(":")) keyStr = "minecraft:" + keyStr;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.playSound(net.kyori.adventure.sound.Sound.sound(
                            net.kyori.adventure.key.Key.key(keyStr),
                            net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f));
                }
            } catch (Throwable ignored) {}
        }
    }
}
