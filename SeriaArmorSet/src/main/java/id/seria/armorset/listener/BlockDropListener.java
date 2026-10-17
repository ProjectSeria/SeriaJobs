package id.seria.armorset.listener;

import id.seria.armorset.SeriaArmorSetPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;

public class BlockDropListener implements Listener {

    private final SeriaArmorSetPlugin plugin;

    public BlockDropListener(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        plugin.getSkillManager().dispatchBlockDrop(player, event);
    }
}