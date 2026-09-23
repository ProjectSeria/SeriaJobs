package id.seria.gacha.lib.common.utils.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class CooldownHandler {
   private final Map<UUID, Long> cooldowns = new HashMap<>();

   public CooldownHandler() {
   }

   public boolean checkAndUseCooldown(Player player, double seconds) {
      if (this.haveCooldown(player)) {
         return false;
      }

      this.addCooldown(player, seconds);
      return true;
   }

   public void addCooldown(Player player, double seconds) {
      this.cooldowns.put(player.getUniqueId(), (long)(System.currentTimeMillis() + seconds * 1000.0));
   }

   public boolean haveCooldown(Player player) {
      long cachedCooldown = this.cooldowns.getOrDefault(player.getUniqueId(), 0L);
      if (System.currentTimeMillis() > cachedCooldown) {
         this.cooldowns.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   public double getSecondsLeft(Player player) {
      if (this.haveCooldown(player)) {
         long cachedCooldown = this.cooldowns.getOrDefault(player.getUniqueId(), 0L);
         double leftMillis = cachedCooldown - System.currentTimeMillis();
         return leftMillis / 1000.0;
      } else {
         return 0.0;
      }
   }
}
