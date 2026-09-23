package id.seria.gacha.limbo.hooks.placeholders;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;

class CrateRewardMatch {
   final CrateType crateType;
   final CustomReward reward;

   private CrateRewardMatch(CrateType crateType, CustomReward reward) {
      this.crateType = crateType;
      this.reward = reward;
   }

   static CrateRewardMatch resolve(PhoenixCrates plugin, String identifier) {
      for (CrateType type : plugin.getCratesManager().getRegisteredTypes()) {
         if (identifier.startsWith(type.getIdentifier() + "_")) {
            String rewardId = identifier.substring(type.getIdentifier().length() + 1);
            CustomReward reward = type.getRewardByIdentifier(rewardId);
            if (reward != null) {
               return new CrateRewardMatch(type, reward);
            }
         }
      }

      return null;
   }
}
