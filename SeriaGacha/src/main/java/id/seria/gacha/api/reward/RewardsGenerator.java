package id.seria.gacha.api.reward;

import id.seria.gacha.api.key.Key;
import id.seria.gacha.api.sdk.exceptions.NotImplementedException;
import java.util.List;

public interface RewardsGenerator {
   List<? extends BaseReward> generateRewards();

   @Deprecated
   default <T extends BaseReward> T generateReward(Key key) {
      throw new NotImplementedException();
   }
}
