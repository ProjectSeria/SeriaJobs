package id.seria.gacha.lib.common.utils.cooldown;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.UUID;
import org.bukkit.entity.Player;

public class Cooldowns {
   private Table<UUID, String, Long> cooldowns = HashBasedTable.create();

   public Cooldowns() {
   }

   public boolean checkAndUseCooldown(Player player, String identifier, double seconds) {
      if (this.haveCooldown(player, identifier)) {
         return false;
      }

      this.addCooldown(player, identifier, seconds);
      return true;
   }

   public void addCooldown(Player player, String identifier, double seconds) {
      this.cooldowns.put(player.getUniqueId(), identifier, (long)(System.currentTimeMillis() + seconds * 1000.0));
   }

   public void removeCooldown(Player player, String identifier) {
      this.cooldowns.remove(player.getUniqueId(), identifier);
   }

   public boolean haveCooldown(Player player, String identifier) {
      Long cachedCooldown = (Long)this.cooldowns.get(player.getUniqueId(), identifier);
      if (cachedCooldown == null) {
         return false;
      } else if (System.currentTimeMillis() > cachedCooldown) {
         this.cooldowns.remove(player.getUniqueId(), identifier);
         return false;
      } else {
         return true;
      }
   }
}
