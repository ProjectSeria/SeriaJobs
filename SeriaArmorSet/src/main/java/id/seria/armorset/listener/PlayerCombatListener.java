package id.seria.armorset.listener;

import id.seria.armorset.SeriaArmorSetPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerCombatListener implements Listener {

    private final SeriaArmorSetPlugin plugin;

    public PlayerCombatListener(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 1. Defender check (Armor set wearer receiving damage)
        if (event.getEntity() instanceof Player) {
            Player defender = (Player) event.getEntity();
            plugin.getSkillManager().dispatchDamagedByEntity(defender, event);
        }

        // 2. Attacker check (Armor set wearer attacking an entity)
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            plugin.getSkillManager().dispatchAttackEntity(attacker, event);
        }
    }
}