package id.seria.armorset.hook;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.config.ArmorSetConfig;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MythicMobsHook {

    private final SeriaArmorSetPlugin plugin;
    private boolean available = false;
    private final Set<UUID> handledEntities = ConcurrentHashMap.newKeySet();

    public MythicMobsHook(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            try {
                Bukkit.getPluginManager().registerEvents(new MythicMobsDeathListener(), plugin);
                available = true;
                plugin.getLogger().info("Hooked into MythicMobs (custom drop tables support enabled)!");
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to initialize MythicMobs hook: " + t.getMessage());
            }
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isHandled(UUID entityUuid) {
        return handledEntities.contains(entityUuid);
    }

    private class MythicMobsDeathListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onMythicMobDeath(MythicMobDeathEvent event) {
            ArmorSetConfig config = plugin.getConfiguration();
            if (!config.isMobsEnabled()) return;

            LivingEntity killerEntity = event.getKiller();
            if (!(killerEntity instanceof Player)) return;

            Player killer = (Player) killerEntity;
            if (!plugin.getMmoItemsHook().hasActiveSet(killer)) return;

            List<ItemStack> drops = event.getDrops();
            if (drops == null || drops.isEmpty()) return;

            UUID entityUuid = event.getEntity().getUniqueId();
            handledEntities.add(entityUuid);
            Bukkit.getScheduler().runTaskLater(plugin, () -> handledEntities.remove(entityUuid), 40L);

            List<ItemStack> leftovers = new ArrayList<>();
            boolean anyAdded = false;

            for (ItemStack drop : drops) {
                if (drop == null || drop.getType().isAir()) continue;

                Map<Integer, ItemStack> remaining = killer.getInventory().addItem(drop);
                if (remaining.isEmpty()) {
                    anyAdded = true;
                } else {
                    leftovers.addAll(remaining.values());
                    if (remaining.values().iterator().next().getAmount() < drop.getAmount()) {
                        anyAdded = true;
                    }
                }
            }

            event.setDrops(leftovers);

            if (anyAdded && config.isPlaySound()) {
                killer.playSound(killer.getLocation(), config.getSound(), config.getSoundVolume(), config.getSoundPitch());
            }

            if (config.isDebug()) {
                plugin.getLogger().info("[Debug] Intercepted MythicMob drops for killer " + killer.getName()
                        + " (Processed: " + (drops.size() - leftovers.size()) + " items, Leftovers: " + leftovers.size() + ")");
            }
        }
    }
}