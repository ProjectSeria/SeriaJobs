package id.seria.gacha.managers.crates.animations.opening.phases.third;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.internal.RewardsDisplay;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.util.List;

public class ThirdPhaseNoneAnimation extends PhaseRunnerImpl {
   private final RewardsDisplay rewardsDisplay = new RewardsDisplay(
      this, this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()))
   );
   private int alpha;

   public ThirdPhaseNoneAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      List<? extends BaseReward> rewards = this.handler.generateRewards();
      this.rewardsDisplay.create(rewards);
      this.session.giveRewards(rewards);
   }

   @Override
   public void tick() {
      if (this.alpha == 70) {
         this.nextPhase();
      }

      this.alpha++;
   }

   @Override
   public void unload() {
      this.rewardsDisplay.destroy();
      this.closeCrate();
      this.playSound(this.animationLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 0.2F);
   }
}
