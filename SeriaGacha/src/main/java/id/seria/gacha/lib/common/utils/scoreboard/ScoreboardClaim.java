package id.seria.gacha.lib.common.utils.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public final class ScoreboardClaim {
   private static final String KEY = "phoenix-scoreboard-claim";

   private ScoreboardClaim() {
   }

   public static void claim(Player player, Plugin owner) {
      player.setMetadata("phoenix-scoreboard-claim", new FixedMetadataValue(owner, true));
   }

   public static void release(Player player, Plugin owner) {
      player.removeMetadata("phoenix-scoreboard-claim", owner);
   }

   public static boolean isClaimedByOther(Player player, Plugin self) {
      for (MetadataValue value : player.getMetadata("phoenix-scoreboard-claim")) {
         Plugin owner = value.getOwningPlugin();
         if (owner != null && !owner.equals(self) && owner.isEnabled()) {
            return true;
         }
      }

      return false;
   }
}
