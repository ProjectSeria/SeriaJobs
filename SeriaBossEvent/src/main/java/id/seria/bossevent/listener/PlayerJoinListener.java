package id.seria.bossevent.listener;

import id.seria.bossevent.SeriaBossEventPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final SeriaBossEventPlugin plugin;

    public PlayerJoinListener(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        List<String> pending = plugin.getResultStore().pollPendingRewards(uuid);
        if (pending != null && !pending.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    for (String cmd : pending) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }
                    plugin.getLogger().info("[SeriaBossEvent] Dispatched " + pending.size() + " pending reward commands for " + player.getName());
                } else {
                    // Put back if went offline again
                    plugin.getResultStore().addPendingReward(uuid, pending);
                }
            }, 1L);
        }
    }
}
