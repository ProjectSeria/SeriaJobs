package id.seria.bossevent.listener;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.event.BossEvent;
import id.seria.bossevent.tracker.FightTracker;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public class BossListener implements Listener {

    private final SeriaBossEventPlugin plugin;

    public BossListener(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicDeath(MythicMobDeathEvent event) {
        if (event.getMob() == null || event.getMob().getEntity() == null) return;

        Entity entity = event.getMob().getEntity().getBukkitEntity();
        if (entity == null) return;

        UUID uuid = entity.getUniqueId();
        FightTracker tracker = plugin.getHpPollTask().getTracker(uuid);
        if (tracker == null || tracker.isDead()) return;

        tracker.setDead(true);

        // 7.4 Lethal blow calculation: record final damage delta
        double lastHp = tracker.getLastHealth();
        if (lastHp > 0 && tracker.getLastDamager() != null) {
            tracker.addDamage(tracker.getLastDamager(), lastHp);
        }
        tracker.setLastHealth(0.0);

        // Determine killer
        Player killer = null;
        if (event.getKiller() instanceof Player p) {
            killer = p;
        } else if (entity instanceof LivingEntity living && living.getKiller() != null) {
            killer = living.getKiller();
        }

        // 7.6 Uncontested check: Void, Kill, WorldBorder, Suicide
        if (entity instanceof LivingEntity living && living.getLastDamageCause() != null) {
            EntityDamageEvent.DamageCause cause = living.getLastDamageCause().getCause();
            if (cause == EntityDamageEvent.DamageCause.VOID ||
                cause == EntityDamageEvent.DamageCause.KILL ||
                cause == EntityDamageEvent.DamageCause.WORLD_BORDER ||
                cause == EntityDamageEvent.DamageCause.SUICIDE) {
                tracker.getDamage().clear(); // Treated as uncontested
            }
        }

        BossEvent bossEvent = plugin.getEventManager().getEvent(tracker.getEventName());
        if (bossEvent != null) {
            bossEvent.onBossKilled(tracker, killer);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMythicDespawn(MythicMobDespawnEvent event) {
        if (event.getMob() == null || event.getMob().getEntity() == null) return;

        Entity entity = event.getMob().getEntity().getBukkitEntity();
        if (entity == null) return;

        UUID uuid = entity.getUniqueId();
        FightTracker tracker = plugin.getHpPollTask().getTracker(uuid);
        if (tracker == null || tracker.isDead()) return;

        // Grace period 5 seconds (100 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            FightTracker current = plugin.getHpPollTask().getTracker(uuid);
            if (current != null && !current.isDead()) {
                Entity currentEntity = Bukkit.getEntity(uuid);
                if (currentEntity == null || !currentEntity.isValid() || currentEntity.isDead()) {
                    BossEvent bossEvent = plugin.getEventManager().getEvent(tracker.getEventName());
                    if (bossEvent != null) {
                        bossEvent.onBossDespawnFailed(tracker);
                    }
                }
            }
        }, 100L);
    }
}
