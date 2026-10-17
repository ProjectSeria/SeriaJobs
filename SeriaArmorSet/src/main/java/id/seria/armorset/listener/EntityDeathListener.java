package id.seria.armorset.listener;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.config.ArmorSetConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityDeathListener implements Listener {

    private final SeriaArmorSetPlugin plugin;

    public EntityDeathListener(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        ArmorSetConfig config = plugin.getConfiguration();
        if (!config.isMobsEnabled()) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        if (!plugin.getMmoItemsHook().hasActiveSet(killer)) return;

        List<ItemStack> drops = event.getDrops();
        if (drops != null && !drops.isEmpty()) {
            List<ItemStack> leftovers = new ArrayList<>();
            boolean anyAdded = false;

            for (ItemStack drop : drops) {
                if (drop == null || drop.getType().isAir()) continue;

                int originalAmount = drop.getAmount();
                Map<Integer, ItemStack> remaining = killer.getInventory().addItem(drop);

                if (remaining.isEmpty()) {
                    anyAdded = true;
                } else {
                    leftovers.addAll(remaining.values());
                    if (remaining.values().iterator().next().getAmount() < originalAmount) {
                        anyAdded = true;
                    }
                }
            }

            drops.clear();
            drops.addAll(leftovers);

            if (anyAdded && config.isPlaySound()) {
                killer.playSound(killer.getLocation(), config.getSound(), config.getSoundVolume(), config.getSoundPitch());
            }

            if (config.isDebug()) {
                plugin.getLogger().info("[Debug] Transferred mob drops to killer " + killer.getName()
                        + " (Remaining on ground: " + leftovers.size() + ")");
            }
        }

        if (config.isXpEnabled() && event.getDroppedExp() > 0) {
            killer.giveExp(event.getDroppedExp());
            event.setDroppedExp(0);
        }
    }
}