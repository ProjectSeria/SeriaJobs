package id.seria.bossevent.tracker;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.config.EventConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HpPollTask {

    private final SeriaBossEventPlugin plugin;
    private final Map<UUID, FightTracker> activeTrackers = new ConcurrentHashMap<>();
    private BukkitTask task;

    public HpPollTask(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void start() {
        if (task != null && !task.isCancelled()) {
            return;
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeTrackers.isEmpty()) return;

                for (Map.Entry<UUID, FightTracker> entry : activeTrackers.entrySet()) {
                    UUID uuid = entry.getKey();
                    FightTracker tracker = entry.getValue();

                    if (tracker.isDead()) {
                        activeTrackers.remove(uuid);
                        continue;
                    }

                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity == null || !entity.isValid() || entity.isDead()) {
                        continue;
                    }

                    if (entity instanceof LivingEntity living) {
                        pollLivingEntity(tracker, living);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L); // Runs every 1 tick
    }

    public void pollLivingEntity(FightTracker tracker, LivingEntity living) {
        double current = living.getHealth();
        double last = tracker.getLastHealth();

        if (current < last) {
            double delta = last - current;
            UUID lastDamager = tracker.getLastDamager();
            if (lastDamager != null) {
                tracker.addDamage(lastDamager, delta);
            } else {
                EventConfig cfg = plugin.getEventConfigLoader().getEvent(tracker.getEventName());
                if (cfg != null && cfg.isAttributeUnownedDamage()) {
                    // attribute unowned damage if configured
                }
            }
        }

        tracker.setLastHealth(current);
    }

    public synchronized void registerTracker(FightTracker tracker) {
        activeTrackers.put(tracker.getBossUuid(), tracker);
    }

    public synchronized void unregisterTracker(UUID uuid) {
        activeTrackers.remove(uuid);
    }

    public FightTracker getTracker(UUID uuid) {
        return activeTrackers.get(uuid);
    }

    public Map<UUID, FightTracker> getActiveTrackers() {
        return activeTrackers;
    }

    public synchronized void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        activeTrackers.clear();
    }
}
