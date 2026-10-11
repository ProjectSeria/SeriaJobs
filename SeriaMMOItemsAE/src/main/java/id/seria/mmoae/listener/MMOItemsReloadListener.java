package id.seria.mmoae.listener;

import id.seria.mmoae.SeriaMMOItemsAEPlugin;
import id.seria.mmoae.hook.MMOItemsHook;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * Listens for MMOItems reload commands (/mi reload, /mmoitems reload)
 * to automatically re-process templates and resolve placeholders in item names.
 */
public class MMOItemsReloadListener implements Listener {

    private final SeriaMMOItemsAEPlugin plugin;

    public MMOItemsReloadListener(SeriaMMOItemsAEPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase().trim();
        if (msg.startsWith("/mi reload") || msg.startsWith("/mmoitems reload")) {
            scheduleTemplateReprocess();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        String cmd = event.getCommand().toLowerCase().trim();
        if (cmd.startsWith("mi reload") || cmd.startsWith("mmoitems reload")) {
            scheduleTemplateReprocess();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(org.bukkit.event.server.ServerLoadEvent event) {
        // Automatically runs once the server has fully finished booting and all plugins are enabled
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (MMOItemsHook.isAvailable()) {
                MMOItemsHook.preProcessAllTemplates();
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mmoitems reload");
                } catch (Throwable ignored) {}
            }
        }, 10L);
    }

    private void scheduleTemplateReprocess() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (MMOItemsHook.isAvailable()) {
                MMOItemsHook.preProcessAllTemplates();
            }
        }, 5L);
    }
}
