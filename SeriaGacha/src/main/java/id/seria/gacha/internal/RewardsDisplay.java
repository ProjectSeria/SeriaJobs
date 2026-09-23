package id.seria.gacha.internal;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeItemEntity;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.lib.common.utils.utility.RotationUtil;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class RewardsDisplay {
   private final PhaseRunnerImpl phaseAnimation;
   private final Location location;
   private final List<RewardsDisplay.RewardComponent> rewards = new ArrayList<>();
   private Task rotationTask;

   @Deprecated
   public RewardsDisplay(PhaseRunnerImpl phaseAnimation) {
      this(phaseAnimation, phaseAnimation.getAnimationLocation().clone().add(0.0, 3.0, 0.0));
   }

   public RewardsDisplay(PhaseRunnerImpl phaseAnimation, Location location) {
      this.phaseAnimation = phaseAnimation;
      this.location = location;
   }

   public void create(List<? extends BaseReward> items) {
      this.destroy();
      if (!items.isEmpty()) {
         if (items.size() == 1) {
            this.createSingleReward(items.get(0));
         } else {
            this.createMultipleRewardsDisplay(items);
         }
      }
   }

   private void createSingleReward(BaseReward reward) {
      double f = 1.5;
      FakeArmorStandEntity holderEntity = Utilities.getNMSFactory()
         .createFakeArmorStand(PhoenixCrates.getLegacyInstance().getOriginal(), this.location.clone().add(0.0, -1.5, 0.0));
      holderEntity.setGravity(false);
      holderEntity.setInvisible(true);
      holderEntity.setAudience(this.phaseAnimation.getAudience());
      FakeItemEntity itemEntity = Utilities.getNMSFactory()
         .createFakeItem(PhoenixCrates.getLegacyInstance().getOriginal(), this.location.clone().add(0.0, 0.0, 0.0));
      itemEntity.setItemStack(reward.getDisplayItem().build());
      itemEntity.setAudience(this.phaseAnimation.getAudience());
      Services.getWatcherService().startWatching(holderEntity, false);
      Services.getWatcherService().startWatching(itemEntity, false);
      holderEntity.setPassenger(itemEntity);
      this.rewards.add(new RewardsDisplay.RewardComponent(holderEntity, itemEntity));
      FakeArmorStandEntity nameEntity = Utilities.getNMSFactory()
         .createFakeArmorStand(PhoenixCrates.getLegacyInstance().getOriginal(), this.applyNameOffset(itemEntity.getLocation()));
      nameEntity.setGravity(false);
      nameEntity.setInvisible(true);
      nameEntity.setMarker(true);
      nameEntity.setCustomName(this.getSingleRewardName(reward));
      nameEntity.setCustomNameVisible(true);
      nameEntity.setAudience(this.phaseAnimation.getAudience());
      Services.getWatcherService().startWatching(nameEntity, false);
      this.rewards.add(new RewardsDisplay.RewardComponent(nameEntity, null));
   }

   private Location applyNameOffset(Location base) {
      Vector offset = this.phaseAnimation.getCrate().getType().getExtraSettings().getDisplayItemNamePosition().clone();
      double facingYaw = this.phaseAnimation.getCrate().getFacingDirection().getDirection();
      RotationUtil.applyYawRot(offset, -facingYaw);
      return base.clone().add(offset);
   }

   private void createMultipleRewardsDisplay(List<? extends BaseReward> items) {
      double f = 1.5;
      FakeArmorStandEntity holderEntity = Utilities.getNMSFactory()
         .createFakeArmorStand(PhoenixCrates.getLegacyInstance().getOriginal(), this.location.clone().add(0.0, -1.5, 0.0));
      holderEntity.setGravity(false);
      holderEntity.setInvisible(true);
      holderEntity.setAudience(this.phaseAnimation.getAudience());
      FakeItemEntity itemEntity = Utilities.getNMSFactory()
         .createFakeItem(PhoenixCrates.getLegacyInstance().getOriginal(), this.location.clone().add(0.0, 0.30000000000000004, 0.0));
      itemEntity.setItemStack(items.get(0).getDisplayItem().build());
      itemEntity.setCustomNameVisible(false);
      itemEntity.setAudience(this.phaseAnimation.getAudience());
      Services.getWatcherService().startWatching(holderEntity, false);
      Services.getWatcherService().startWatching(itemEntity, false);
      holderEntity.setPassenger(itemEntity);
      this.rewards.add(new RewardsDisplay.RewardComponent(holderEntity, itemEntity));
      StringBuilder firstLineText = new StringBuilder();
      if (!this.phaseAnimation.getCrate().getType().isSimultaneousOpeningsEnabled()) {
         firstLineText.append("\u00a7e").append(this.phaseAnimation.getPlayer().getName()).append(" \u00a77got: ");
      }

      int displayCount = Math.min(3, items.size());
      List<String> rewardNames = new ArrayList<>();
      int totalLength = 0;

      for (int i = 0; i < displayCount; i++) {
         BaseReward reward = items.get(i);
         String rewardName = BukkitUtil.extractName(reward.getDisplayItem().build());
         int amount = reward.getDisplayItem().getAmount();
         String formattedReward = "\u00a7e" + amount + "x \u00a7f" + rewardName;
         int rewardLength = formattedReward.replaceAll("\u00a7.", "").length();
         if (totalLength + rewardLength > 45 && !rewardNames.isEmpty()) {
            displayCount = i;
            break;
         }

         rewardNames.add(formattedReward);
         totalLength += rewardLength + 2;
      }

      firstLineText.append(String.join("\u00a77, ", rewardNames));
      FakeArmorStandEntity firstLineEntity = Utilities.getNMSFactory()
         .createFakeArmorStand(PhoenixCrates.getLegacyInstance().getOriginal(), this.location.clone().add(0.0, -0.10000000000000009, 0.0));
      firstLineEntity.setGravity(false);
      firstLineEntity.setInvisible(true);
      firstLineEntity.setMarker(true);
      firstLineEntity.setCustomName(firstLineText.toString());
      firstLineEntity.setCustomNameVisible(true);
      firstLineEntity.setAudience(this.phaseAnimation.getAudience());
      Services.getWatcherService().startWatching(firstLineEntity, false);
      this.rewards.add(new RewardsDisplay.RewardComponent(firstLineEntity, null));
      if (items.size() > displayCount) {
         int remainingCount = items.size() - displayCount;
         String secondLineText = "\u00a77and " + remainingCount + " more...";
         FakeArmorStandEntity secondLineEntity = Utilities.getNMSFactory()
            .createFakeArmorStand(PhoenixCrates.getLegacyInstance().getOriginal(), this.location.clone().add(0.0, -0.3999999999999999, 0.0));
         secondLineEntity.setGravity(false);
         secondLineEntity.setInvisible(true);
         secondLineEntity.setMarker(true);
         secondLineEntity.setCustomName(secondLineText);
         secondLineEntity.setCustomNameVisible(true);
         secondLineEntity.setAudience(this.phaseAnimation.getAudience());
         Services.getWatcherService().startWatching(secondLineEntity, false);
         this.rewards.add(new RewardsDisplay.RewardComponent(secondLineEntity, null));
      }

      this.startItemRotation(itemEntity, items);
   }

   public void tick() {
   }

   private void startItemRotation(FakeItemEntity itemEntity, List<? extends BaseReward> items) {
      int[] currentIndex = new int[]{0};
      this.rotationTask = PhoenixCrates.getLegacyInstance().getScheduler().scheduleSync(() -> {
         if (!this.rewards.isEmpty()) {
            currentIndex[0] = (currentIndex[0] + 1) % items.size();
            itemEntity.setItemStack(items.get(currentIndex[0]).getDisplayItem().build());
         }
      }, 10L, 10L);
   }

   private String getSingleRewardName(BaseReward reward) {
      return this.phaseAnimation.getCrate().getType().isSimultaneousOpeningsEnabled()
         ? Translations.t(
            "crate.reward-got", "%amount%", reward.getDisplayItem().getAmount(), "%reward%", BukkitUtil.extractName(reward.getDisplayItem().build())
         )[0]
         : Translations.t(
            "crate.player-got",
            "%player%",
            this.phaseAnimation.getPlayer().getName(),
            "%reward%",
            BukkitUtil.extractName(reward.getDisplayItem().build()),
            "%amount%",
            reward.getDisplayItem().getAmount(),
            "%crate%",
            this.phaseAnimation.getCrate().getType().getFormattedDisplayName(null)
         )[0];
   }

   public void destroy() {
      if (this.rotationTask != null) {
         this.rotationTask.cancel();
         this.rotationTask = null;
      }

      for (RewardsDisplay.RewardComponent component : this.rewards) {
         if (component.itemEntity != null) {
            Services.getWatcherService().stopWatching(component.itemEntity);
         }

         Services.getWatcherService().stopWatching(component.holderEntity);
      }

      this.rewards.clear();
   }

   public static class RewardComponent {
      private FakeArmorStandEntity holderEntity;
      private FakeItemEntity itemEntity;

      @Generated
      public FakeArmorStandEntity getHolderEntity() {
         return this.holderEntity;
      }

      @Generated
      public FakeItemEntity getItemEntity() {
         return this.itemEntity;
      }

      @Generated
      public RewardComponent(FakeArmorStandEntity holderEntity, FakeItemEntity itemEntity) {
         this.holderEntity = holderEntity;
         this.itemEntity = itemEntity;
      }
   }
}
