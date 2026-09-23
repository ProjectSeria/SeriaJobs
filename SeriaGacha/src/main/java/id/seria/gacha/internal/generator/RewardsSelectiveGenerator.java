package id.seria.gacha.internal.generator;

import com.google.common.collect.Lists;
import id.seria.gacha.api.key.Key;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.reward.RewardsGenerator;
import id.seria.gacha.api.session.OpeningContext;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.reward.WrappedReward;
import java.util.ArrayList;
import java.util.List;

public class RewardsSelectiveGenerator implements RewardsGenerator {
   private final Crate crate;
   private final OpeningContext.SelectiveOpeningContext data;
   private boolean generated;

   public RewardsSelectiveGenerator(Crate crate, OpeningContext.SelectiveOpeningContext data) {
      this.crate = crate;
      this.data = data;
   }

   @Override
   public List<? extends BaseReward> generateRewards() {
      if (this.data.getSelectedReward().isRestricted(this.data.getPlayer())) {
         throw new IllegalArgumentException("Reward is restricted for this player");
      }

      WrappedReward generated = this.generateReward(null);
      return generated == null ? new ArrayList<>() : Lists.newArrayList(new WrappedReward[]{generated});
   }

   public WrappedReward generateReward(Key key) {
      if (this.generated) {
         return null;
      }

      this.generated = true;
      return RewardFacade.wrap(this.data.getSelectedReward());
   }
}
