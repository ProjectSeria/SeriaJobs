package id.seria.gacha.registries.rewards;

import id.seria.gacha.api.reward.BaseReward;
import java.util.List;
import org.bukkit.entity.Player;

@Deprecated
public final class RewardChanceContext {
   private final PipelineContext<BaseReward> value;

   public RewardChanceContext(BaseReward reward, List<? extends BaseReward> rewards, Player viewer) {
      this.value = PipelineContext.of(reward).put("availableRewards", rewards).put("viewer", viewer);
   }

   public PipelineContext<BaseReward> getValue() {
      return this.value;
   }
}
