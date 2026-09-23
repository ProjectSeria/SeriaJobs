package id.seria.gacha.managers.crates.animations.opening.phases.third;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.internal.RewardsDisplay;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import java.util.List;
import org.bukkit.Location;

public class ThirdPhaseHelixAnimation extends PhaseRunnerImpl {
   private static final int DISPLAY_TICKS = 70;
   private static final int STRAND_COUNT = 2;
   private static final double MAX_HEIGHT = 3.0;
   private final RewardsDisplay rewardsDisplay;
   private final Color primaryColor = this.properties.getColorOrDefault("color", Color.WHITE);
   private int alpha;
   private double delta;

   public ThirdPhaseHelixAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
      this.rewardsDisplay = new RewardsDisplay(
         this, this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()))
      );
   }

   @Override
   public void load() {
      Location temp = this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()));
      ParticleEffect.EXPLOSION_LARGE.display(temp, 0.6F, 0.6F, 0.6F, 0.0F, 4, null, this.getAudience());
      ParticleEffect.SMOKE_NORMAL.display(temp, 0.2F, 0.2F, 0.2F, 0.4F, 30, null, this.getAudience());
      this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 0.8F, 1.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_ENDER_DRAGON_GROWL, 0.3F, 1.0F);
      List<? extends BaseReward> rewards = this.handler.generateRewards();
      this.rewardsDisplay.create(rewards);
      this.session.giveRewards(rewards);
   }

   @Override
   public void tick() {
      double progress = this.alpha / 70.0;
      double radius = 0.8 + this.sin(this.delta * 0.3) * 0.25;

      for (double y = 0.0; y < 3.0; y += 0.5) {
         double heightFactor = y / 3.0;

         for (int s = 0; s < 2; s++) {
            double angle = this.delta + Math.PI * s + y * 1.5;
            double strandRadius = radius * (1.0 - heightFactor * 0.3);
            ParticleEffect.FIREWORKS_SPARK
               .display(
                  this.animationLocation.clone().add(this.sin(angle) * strandRadius, y, this.cos(angle) * strandRadius),
                  0.0F,
                  0.0F,
                  0.0F,
                  0.0F,
                  1,
                  null,
                  this.getAudience()
               );
         }
      }

      if (this.alpha % 3 == 0) {
         double bridgeY = this.alpha * 0.4 % 3.0;
         ParticleEffect.END_ROD.display(this.animationLocation.clone().add(0.0, bridgeY, 0.0), 0.0F, 0.0F, 0.0F, 0.01F, 1, null, this.getAudience());
      }

      if (this.alpha % 3 == 0) {
         double ringAngle = this.delta * 2.0;
         ParticleEffect.REDSTONE
            .display(
               this.animationLocation.clone().add(this.sin(ringAngle) * radius, 0.05, this.cos(ringAngle) * radius),
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               1,
               new RegularColor(this.primaryColor),
               this.getAudience()
            );
         ParticleEffect.REDSTONE
            .display(
               this.animationLocation.clone().add(this.sin(ringAngle + Math.PI) * radius, 0.05, this.cos(ringAngle + Math.PI) * radius),
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               1,
               new RegularColor(this.primaryColor),
               this.getAudience()
            );
      }

      if (this.alpha % 10 == 0) {
         this.playSound(this.animationLocation, XSound.ENTITY_ENDER_DRAGON_FLAP, 0.3F, 1.0F);
      }

      if (this.alpha >= 70) {
         this.nextPhase();
      }

      this.delta += 0.15;
      this.alpha++;
   }

   @Override
   public void unload() {
      this.rewardsDisplay.destroy();
      this.closeCrate();
      this.playSound(this.animationLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 0.2F);
   }
}
