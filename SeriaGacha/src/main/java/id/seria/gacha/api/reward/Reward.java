package id.seria.gacha.api.reward;

import java.util.List;
import org.bukkit.entity.Player;

public interface Reward extends BaseReward {
   @Override
   String getIdentifier();

   @Override
   double getWeight();

   int getWinLimits();

   List<String> getRestrictedPermissions();

   default boolean isRestricted(Player player) {
      if (player.isOp()) {
         return false;
      }

      for (String permission : this.getRestrictedPermissions()) {
         if (player.hasPermission(permission)) {
            return true;
         }
      }

      return false;
   }

   int getRequiredKeys();

   AlternativeReward getAlternative();

   @Override
   default boolean isAlternative() {
      return false;
   }

   @Deprecated
   default double getPercentage() {
      return this.getWeight();
   }
}
