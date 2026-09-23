package id.seria.gacha.managers.crates.animations.opening.phases.first;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import org.bukkit.Location;

public class FirstPhaseCycloneHeartAnimation extends PhaseRunnerImpl {
   private int delta;
   private double alpha;
   private double theta;

   public FirstPhaseCycloneHeartAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.delta = 0;
      this.theta = 0.0;
      this.alpha = 2.0;
      this.playSound(this.animationLocation, XSound.BLOCK_BAMBOO_PLACE, 1.0F, 0.1F);
      this.playSound(this.animationLocation, XSound.ENTITY_CHICKEN_EGG, 0.5F, 1.0F);

      for (double t = 0.0; t <= Math.PI * 2; t += Math.PI * 2.0 / 7.0) {
         Location pos = this.getCenterLocation().clone().add(MathUtil.sin(t) * this.alpha, -0.5, MathUtil.cos(t) * this.alpha);
         ParticleEffect.HEART.display(pos, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
      }
   }

   @Override
   public void tick() {
      if (this.delta > 2) {
         for (double t = 0.0; t <= Math.PI * 2; t += Math.PI * 2.0 / 7.0) {
            Location pos = this.getCenterLocation().clone().add(MathUtil.sin(t + this.theta) * this.alpha, 0.0, MathUtil.cos(t + this.theta) * this.alpha);
            ParticleEffect.REDSTONE.display(pos, 0.0F, 0.0F, 0.0F, 0.0F, 1, new RegularColor(Color.RED), this.getAudience());
         }

         this.alpha -= 0.11;
         this.theta += 0.1;
      }

      if (this.delta > 18) {
         this.playSound(this.animationLocation, XSound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
         ParticleEffect.HEART.display(this.getAnimationLocation(), 0.5F, 0.5F, 0.5F, 0.0F, 10, null, this.getAudience());
         this.nextPhase();
      }

      this.delta++;
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
