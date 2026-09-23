package id.seria.gacha.managers.crates.rewards;

import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;

public final class GuaranteedRewardCalculator {
   public static final int NO_REMAINING = Integer.MAX_VALUE;

   private GuaranteedRewardCalculator() {
   }

   public static int getRemainingOpenings(CrateType crateType, CustomReward reward, int totalOpenings) {
      if (!crateType.isGuaranteedRewardDisabled() && reward.getGuaranteedWin() > 0) {
         switch (crateType.getGuaranteedRewardType()) {
            case SEQUENTIAL:
               return reward.getGuaranteedWin() - totalOpenings;
            case REPETITIVE:
               return reward.getGuaranteedWin() - totalOpenings % reward.getGuaranteedWin();
            default:
               return Integer.MAX_VALUE;
         }
      } else {
         return Integer.MAX_VALUE;
      }
   }

   public static int getMinRemaining(CrateType crateType, int totalOpenings) {
      int minRemaining = Integer.MAX_VALUE;

      for (CustomReward reward : crateType.getRegisteredRewards()) {
         int remaining = getRemainingOpenings(crateType, reward, totalOpenings);
         if (remaining > 0 && remaining < minRemaining) {
            minRemaining = remaining;
         }
      }

      return minRemaining;
   }

   public static CustomReward getClosestReward(CrateType crateType, int totalOpenings) {
      CustomReward closest = null;
      int minRemaining = Integer.MAX_VALUE;

      for (CustomReward reward : crateType.getRegisteredRewards()) {
         int remaining = getRemainingOpenings(crateType, reward, totalOpenings);
         if (remaining > 0 && remaining < minRemaining) {
            minRemaining = remaining;
            closest = reward;
         }
      }

      return closest;
   }
}
