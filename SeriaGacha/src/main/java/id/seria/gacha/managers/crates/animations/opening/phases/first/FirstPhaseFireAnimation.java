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

public class FirstPhaseFireAnimation extends PhaseRunnerImpl {
   private int step;
   private int alpha;
   private double delta;

   public FirstPhaseFireAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.playSound(this.animationLocation, XSound.ENTITY_MAGMA_CUBE_SQUISH, 1.0F, 0.1F);
   }

   @Override
   public void tick() {
      ParticleEffect.SPELL_MOB_AMBIENT
         .display(this.animationLocation.clone().add(0.0, 0.5, 0.0), 0.5F, 0.5F, 0.5F, 0.05F, 1, new RegularColor(Color.RED), this.getAudience());
      switch (this.step) {
         case 0: {
            double r = 1.0 + this.delta;
            double inc = Math.PI / 10;

            for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 10) {
               Location temp = this.animationLocation.clone().add(this.sin(t) * r, 0.1, this.cos(t) * r);
               ParticleEffect.FLAME.display(temp, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
            }

            if (this.alpha == 20) {
               this.playSound(this.animationLocation, XSound.ENTITY_MAGMA_CUBE_DEATH, 1.0F, 2.0F);
               this.playSound(this.animationLocation, XSound.BLOCK_FIRE_EXTINGUISH, 1.0F, 0.8F);
            }

            if (this.alpha > 20) {
               this.delta += 0.05;
               if (this.delta > 1.5) {
                  this.delta = 1.5;
                  this.nextStep();
               }
            }
            break;
         }
         case 1: {
            double r = 1.0 + this.delta;
            double inc = Math.PI / 10;
            if (this.alpha < 20) {
               for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 10) {
                  Location temp = this.animationLocation.clone().add(this.sin(t) * r, 0.1, this.cos(t) * r);
                  ParticleEffect.FLAME.display(temp, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
               }
            } else {
               this.playSound(this.animationLocation, XSound.ENTITY_COD_DEATH, 1.0F, 0.1F);
               this.playSound(this.animationLocation, XSound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
               this.playSound(this.animationLocation, XSound.ENTITY_CREEPER_PRIMED, 1.0F, 1.8F);

               for (double y = 0.0; y <= 4.0; y += 0.1) {
                  for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 2) {
                     Location temp = this.animationLocation.clone().add(this.sin(t + y) * r, y, this.cos(t + y) * r);
                     ParticleEffect.DRIP_LAVA.display(temp, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
                  }
               }

               for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 10) {
                  Location temp = this.animationLocation.clone().add(this.sin(t) * r, 4.0, this.cos(t) * r);
                  ParticleEffect.FLAME.display(temp, 0.0F, -0.3F, 0.0F, 0.3F, 0, null, this.getAudience());
               }

               for (double i = 0.0; i < 10.0; i++) {
                  Location temp = this.animationLocation.clone().add(MathUtil.random(-3.0, 3.0), MathUtil.random(0.0, 2.0), MathUtil.random(-3.0, 3.0));
                  ParticleEffect.SMOKE_NORMAL.display(temp, 0.0F, 0.5F, 0.0F, 0.5F, 0, null, this.getAudience());
               }

               this.nextStep();
            }
            break;
         }
         case 2: {
            double r = 1.0 + this.delta;
            double inc = Math.PI / 10;

            for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 10) {
               Location temp = this.animationLocation.clone().add(this.sin(t) * r, 4.0, this.cos(t) * r);
               ParticleEffect.FLAME.display(temp, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
            }

            if (this.alpha >= 40) {
               this.nextPhase();
            }
            break;
         }
      }

      this.alpha++;
   }

   @Override
   public void unload() {
   }

   private void nextStep() {
      this.step++;
      this.alpha = -1;
   }

   @Override
   public void nextPhase() {
      this.openCrate();
      super.nextPhase();
   }
}
