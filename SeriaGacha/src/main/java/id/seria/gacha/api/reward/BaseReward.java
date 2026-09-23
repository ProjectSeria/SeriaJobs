package id.seria.gacha.api.reward;

import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.sdk.exceptions.NotImplementedException;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public interface BaseReward {
   String getIdentifier();

   @Nullable
   Item getDisplayItem();

   List<? extends Item> getWinItems();

   List<String> getWinCommands();

   double getWeight();

   boolean isBroadcast();

   default boolean isSharePlayersOnline() {
      throw new NotImplementedException();
   }

   default boolean isAllowWinMessage() {
      return true;
   }

   default int getMaxWinItemAmount() {
      return -1;
   }

   default int getMaxWinCommandAmount() {
      return -1;
   }

   boolean isAlternative();
}
