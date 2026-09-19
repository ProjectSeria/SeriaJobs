package id.seria.jobs.manager;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.api.OrderCompleteEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final SeriaJobs plugin;

    public PlayerConnectionListener(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().loadProfileAsync(player.getUniqueId(), player.getName(), profile -> {
            // Profile ready in memory
            if (plugin.getLeaderboardManager() != null) {
                plugin.getLeaderboardManager().checkAndDeliverPendingRewards(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().unloadProfile(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOrderComplete(OrderCompleteEvent event) {
        if (plugin.getLeaderboardManager() != null) {
            plugin.getLeaderboardManager().recordOrderCompletion(event.getPlayer().getUniqueId(), event.getJob().getId());
        }
    }
}

