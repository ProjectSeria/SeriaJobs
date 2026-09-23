package id.seria.gacha.internal.generator;

import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.reward.Reward;
import id.seria.gacha.api.reward.RewardsGenerator;
import id.seria.gacha.api.session.OpeningContext;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.managers.crates.Crate;
import java.util.ArrayList;
import java.util.List;

public class RewardsMassSelectiveGenerator implements RewardsGenerator {
   private final Crate crate;
   private final OpeningContext.MassSelectiveOpeningContext data;
   private boolean generated;

   public RewardsMassSelectiveGenerator(Crate crate, OpeningContext.MassSelectiveOpeningContext data) {
      this.crate = crate;
      this.data = data;
   }

   @Override
   public List<? extends BaseReward> generateRewards() {
      if (this.generated) {
         return new ArrayList<>();
      }

      this.generated = true;
      List<BaseReward> rewards = new ArrayList<>();

      for (Reward reward : this.data.getSelectedRewards()) {
         if (reward.isRestricted(this.data.getPlayer())) {
            throw new IllegalArgumentException("Reward '" + reward.getIdentifier() + "' is restricted for this player");
         }

         rewards.add(RewardFacade.wrap(reward));
      }

      return rewards;
   }
}
