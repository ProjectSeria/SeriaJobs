package id.seria.gacha.managers.crates.animations.opening.phases.first;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.texture.BlockTexture;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;

public class FirstPhaseCrackAnimation extends PhaseRunnerImpl {
   private static final int PHASE_1_END = 20;
   private static final int PHASE_2_END = 40;
   private static final int FINAL_TICK = 57;
   private int alpha;

   public FirstPhaseCrackAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.playSound(this.centerLocation, XSound.BLOCK_STONE_BREAK, 0.3F, 0.5F);
   }

   @Override
   public void tick() {
      this.alpha++;
      BlockTexture blockTexture = new BlockTexture(this.crate.getType().getBlockType().getBukkitMaterial());
      if (this.alpha <= 20) {
         if (this.alpha % 6 == 0) {
            ParticleEffect.BLOCK_CRACK.display(this.centerLocation, 0.2F, 0.2F, 0.2F, 0.05F, 8, blockTexture, this.getAudience());
            this.playSound(this.centerLocation, XSound.BLOCK_STONE_HIT, 0.3F, 1.2F + (float)MathUtil.random(0.0, 0.3));
         }

         if (this.alpha % 4 == 0) {
            ParticleEffect.SMOKE_NORMAL.display(this.centerLocation, 0.15F, 0.15F, 0.15F, 0.01F, 2, null, this.getAudience());
         }
      } else if (this.alpha <= 40) {
         if (this.alpha % 4 == 0) {
            ParticleEffect.BLOCK_CRACK.display(this.centerLocation, 0.35F, 0.35F, 0.35F, 0.1F, 15, blockTexture, this.getAudience());
            ParticleEffect.SMOKE_NORMAL.display(this.centerLocation, 0.3F, 0.2F, 0.3F, 0.02F, 4, null, this.getAudience());
            this.playSound(this.centerLocation, XSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.5F, 0.8F + (float)MathUtil.random(0.0, 0.4));
         }

         if (this.alpha % 5 == 0) {
            double angle = MathUtil.random(0.0, Math.PI * 2);
            double dist = MathUtil.random(0.3, 1.0);
            ParticleEffect.BLOCK_CRACK
               .display(
                  this.animationLocation.clone().add(this.sin(angle) * dist, 0.1, this.cos(angle) * dist),
                  0.1F,
                  0.0F,
                  0.1F,
                  0.02F,
                  5,
                  blockTexture,
                  this.getAudience()
               );
         }
      } else {
         if (this.alpha % 2 == 0) {
            ParticleEffect.BLOCK_CRACK.display(this.centerLocation, 0.5F, 0.5F, 0.5F, 0.2F, 25, blockTexture, this.getAudience());
            ParticleEffect.SMOKE_LARGE.display(this.centerLocation, 0.3F, 0.2F, 0.3F, 0.05F, 3, null, this.getAudience());
         }

         if (this.alpha % 3 == 0) {
            this.playSound(this.centerLocation, XSound.BLOCK_STONE_BREAK, 0.6F, 0.6F + (float)MathUtil.random(0.0, 0.4));
         }

         double ringProgress = (this.alpha - 40) / 17.0;
         double ringRadius = ringProgress * 1.5;
         double inc = Math.PI / 6;

         for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 6) {
            ParticleEffect.BLOCK_CRACK
               .display(
                  this.animationLocation.clone().add(this.sin(t) * ringRadius, 0.05, this.cos(t) * ringRadius),
                  0.0F,
                  0.0F,
                  0.0F,
                  0.0F,
                  2,
                  blockTexture,
                  this.getAudience()
               );
         }
      }

      if (this.alpha == 57) {
         ParticleEffect.BLOCK_CRACK.display(this.centerLocation, 0.6F, 0.8F, 0.6F, 0.3F, 60, blockTexture, this.getAudience());
         ParticleEffect.EXPLOSION_LARGE.display(this.centerLocation, 0.3F, 0.3F, 0.3F, 0.0F, 2, null, this.getAudience());
         this.playSound(this.centerLocation, XSound.ENTITY_GENERIC_EXPLODE, 0.6F, 1.4F);
         this.playSound(this.centerLocation, XSound.BLOCK_GLASS_BREAK, 0.8F, 0.8F);
         this.playSound(this.centerLocation, XSound.BLOCK_STONE_BREAK, 1.0F, 0.5F);
         this.nextPhase();
      }
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
