package id.seria.bossevent.listener;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.tracker.FightTracker;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    private final SeriaBossEventPlugin plugin;

    public DamageListener(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (victim instanceof ComplexEntityPart part) {
            victim = part.getParent();
        }

        FightTracker tracker = plugin.getHpPollTask().getTracker(victim.getUniqueId());
        if (tracker == null || tracker.isDead()) {
            return;
        }

        // Determine player damager
        Player player = null;
        if (event.getDamager() instanceof Player p) {
            player = p;
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player p) {
            player = p;
        }

        if (player == null) return;

        // Cache player metadata
        plugin.getPlayerInfoService().cachePlayer(player);
        Component prefix = plugin.getPlayerInfoService().getPrefix(player);

        // Record aggro tagger
        tracker.recordAggro(player.getUniqueId(), player.getName(), prefix);
    }
}
