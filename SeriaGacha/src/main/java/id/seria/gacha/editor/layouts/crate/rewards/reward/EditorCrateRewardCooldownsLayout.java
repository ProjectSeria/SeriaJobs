package id.seria.gacha.editor.layouts.crate.rewards.reward;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.TimeUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import lombok.Generated;

public class EditorCrateRewardCooldownsLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final CustomReward reward;

   public EditorCrateRewardCooldownsLayout(CrateType crateType, CustomReward reward, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.reward-cooldowns"), parent);
      this.crateType = crateType;
      this.reward = reward;
      this.setViewPersistentOnGoBack(true);
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(3, 2),
               this.createData(
                  XMaterial.GUNPOWDER,
                  Translatable.key("editor.crate.reward.cooldowns.win-limit-cooldown.title"),
                  Translatable.key("editor.crate.reward.cooldowns.win-limit-cooldown.description")
               ),
               new EditorCrateRewardCooldownDurationLayout(
                  this.crateType, this.reward, this, CustomReward::getWinLimitCooldown, CustomReward::setWinLimitCooldown, null
               )
            )
            .addAttribute(this.label("cooldown"), this.formatCooldown(this.reward.getWinLimitCooldown()))
            .addAttribute(this.label("enabled"), this.reward.getWinLimitCooldown() > 0L)
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.NETHERITE_INGOT,
                  Translatable.key("editor.crate.reward.cooldowns.global-win-limit-cooldown.title"),
                  Translatable.key("editor.crate.reward.cooldowns.global-win-limit-cooldown.description")
               ),
               new EditorCrateRewardCooldownDurationLayout(
                  this.crateType, this.reward, this, CustomReward::getGlobalWinLimitCooldown, CustomReward::setGlobalWinLimitCooldown, null
               )
            )
            .addAttribute(this.label("cooldown"), this.formatCooldown(this.reward.getGlobalWinLimitCooldown()))
            .addAttribute(this.label("enabled"), this.reward.getGlobalWinLimitCooldown() > 0L)
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(7, 2),
               this.createData(
                  XMaterial.CLOCK,
                  Translatable.key("editor.crate.reward.cooldowns.time-limited-cooldown.title"),
                  Translatable.key("editor.crate.reward.cooldowns.time-limited-cooldown.description")
               ),
               new EditorCrateRewardCooldownDurationLayout(
                  this.crateType, this.reward, this, CustomReward::getTimeLimitedCooldown, CustomReward::setTimeLimitedCooldown, this::autoStartTimeLimited
               )
            )
            .addAttribute(this.label("cooldown"), this.formatTimeLimited())
            .addAttribute(this.label("enabled"), this.isTimeLimitedActive())
      );
   }

   private void autoStartTimeLimited() {
      long duration = this.reward.getTimeLimitedCooldown();
      if (duration > 0L) {
         this.reward.setTimeLimitedExpiry(System.currentTimeMillis() + duration * 1000L);
         this.reward.setEnabled(true);
         this.crateType.invalidateRewardsCache();
      } else {
         this.reward.setTimeLimitedExpiry(0L);
      }
   }

   private boolean isTimeLimitedActive() {
      return this.reward.getTimeLimitedExpiry() > 0L && this.reward.getTimeLimitedExpiry() > System.currentTimeMillis();
   }

   private String formatTimeLimited() {
      if (this.isTimeLimitedActive()) {
         long remainingMs = this.reward.getTimeLimitedExpiry() - System.currentTimeMillis();
         return TimeUtil.formatSecondsLong(Math.max(1L, remainingMs / 1000L));
      } else {
         return this.formatCooldown(this.reward.getTimeLimitedCooldown());
      }
   }

   private String formatCooldown(long seconds) {
      return seconds <= 0L ? this.t(this.label("disabled"))[0] : TimeUtil.formatSecondsLong(seconds);
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public CustomReward getReward() {
      return this.reward;
   }
}
