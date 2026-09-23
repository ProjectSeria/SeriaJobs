package id.seria.gacha.api.crate;

import id.seria.gacha.api.crate.cost.CostEngineType;
import id.seria.gacha.api.crate.engine.EngineData;
import id.seria.gacha.api.crate.engine.EngineType;
import id.seria.gacha.api.crate.hologram.HologramProperties;
import id.seria.gacha.api.reward.GuaranteedRewardType;
import id.seria.gacha.api.reward.Reward;
import id.seria.gacha.api.sdk.exceptions.NotImplementedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public interface CrateType {
   boolean isEnabled();

   String getIdentifier();

   String getDisplayName();

   CrateBlockType getBlockType();

   EngineType getEngineType();

   EngineData getEngineData();

   double getOpenMoneyCost();

   default CostEngineType getCostEngineType() {
      throw new NotImplementedException();
   }

   int getOpenCooldownSeconds();

   boolean isSimultaneousOpeningsEnabled();

   boolean isPreviewItemEnabled();

   boolean isPermissionRequired();

   String getPermission();

   default HologramProperties getHologramProperties() {
      throw new NotImplementedException();
   }

   @Deprecated
   @ScheduledForRemoval
   default List<String> getHologramLines() {
      Object lines = this.getHologramProperties().get("lines");
      return lines == null ? new ArrayList<>() : (List)lines;
   }

   @Deprecated
   @ScheduledForRemoval
   default double getHologramOffset() {
      return this.getHologramProperties().getOffset();
   }

   String getRewardsMenuName();

   String getSelectRewardMenuName();

   List<String> getBroadcastMessage();

   boolean isKeyRequired();

   Set<String> getLinkedKeysIds();

   GuaranteedRewardType getGuaranteedRewardType();

   default boolean isGuaranteedRewardDisabled() {
      return this.getGuaranteedRewardType() == GuaranteedRewardType.DISABLED;
   }

   List<? extends Reward> getRegisteredRewards();

   List<? extends Reward> getAvailableRewards();

   Reward getRewardByIdentifier(String var1);
}
