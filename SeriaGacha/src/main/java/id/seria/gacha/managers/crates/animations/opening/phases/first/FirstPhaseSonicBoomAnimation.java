package id.seria.gacha.managers.crates.animations.opening.phases.first;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.VibrationData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;

public class FirstPhaseSonicBoomAnimation extends PhaseRunnerImpl {
   private int delta;
   private double alpha;
   private double theta;
   private double k = 0.0;

   public FirstPhaseSonicBoomAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.delta = 0;
      this.alpha = 10.0;
      this.theta = this.alpha;
      this.k = 0.0;
      this.playSound(this.getAnimationLocation(), XSound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.0F);
   }

   @Override
   public void tick() {
      if (this.delta == 25) {
         this.playSound(this.getAnimationLocation(), XSound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.0F);
      }

      if (this.delta == 30) {
         this.playSound(this.getAnimationLocation(), XSound.ENTITY_ITEM_BREAK, 1.0F, 0.1F);
      }

      if (this.delta >= 30) {
         ParticleEffect.SONIC_BOOM.display(this.getAnimationLocation().clone().add(0.0, this.alpha, 0.0), 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
         this.alpha--;
      } else {
         for (double t = 0.0; t <= Math.PI * 2; t += Math.PI * 2.0 / 3.0) {
            ParticleEffect.VIBRATION
               .display(
                  this.getAnimationLocation().clone().add(MathUtil.sin(this.k + t) * 2.0, 0.0, MathUtil.cos(this.k + t) * 2.0),
                  0.0F,
                  0.0F,
                  0.0F,
                  0.0F,
                  1,
                  new VibrationData(
                     this.getAnimationLocation().clone().add(MathUtil.sin(this.k + t) * 2.0, 0.0, MathUtil.cos(this.k + t) * 2.0),
                     this.getAnimationLocation().clone().add(0.0, this.alpha, 0.0),
                     25
                  ),
                  this.getAudience()
               );

            for (double l = 0.0; l <= Math.PI * 2; l++) {
               ParticleEffect.REDSTONE
                  .display(
                     this.getAnimationLocation().clone().add(MathUtil.sin(t + Math.min(this.k, l)) * 2.0, 0.0, MathUtil.cos(t + Math.min(this.k, l)) * 2.0),
                     0.0F,
                     0.0F,
                     0.0F,
                     0.0F,
                     1,
                     new RegularColor(Color.CYAN),
                     this.getAudience()
                  );
            }
         }
      }

      if (this.delta >= 40) {
         ParticleEffect.EXPLOSION_LARGE
            .display(this.getAnimationLocation().clone().add(0.0, this.theta, 0.0), 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
         this.playSound(this.getAnimationLocation(), XSound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
         this.theta--;
      }

      if (this.delta == 55) {
         this.nextPhase();
      }

      this.delta++;
      this.k += 0.1;
   }

   @Override
   public void unload() {
   }

   @Override
   public void nextPhase() {
      this.openCrate();
      super.nextPhase();
   }
}
