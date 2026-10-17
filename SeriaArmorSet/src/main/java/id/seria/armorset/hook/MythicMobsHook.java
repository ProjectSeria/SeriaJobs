package id.seria.armorset.hook;

import id.seria.armorset.SeriaArmorSetPlugin;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MythicMobsHook {

    private final SeriaArmorSetPlugin plugin;
    private boolean available = false;

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

    private class MythicMobsDeathListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onMythicMobDeath(MythicMobDeathEvent event) {
            LivingEntity killerEntity = event.getKiller();
            if (!(killerEntity instanceof Player)) return;

            Player killer = (Player) killerEntity;
            plugin.getSkillManager().dispatchMythicMobDeath(killer, event);
        }
    }
}