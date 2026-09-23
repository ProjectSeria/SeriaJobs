package id.seria.gacha.managers.crates.animations.opening.phases.first;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import org.bukkit.Location;

public class FirstPhaseLightningAnimation extends PhaseRunnerImpl {
   private static final int STRIKE_1_TICK = 15;
   private static final int STRIKE_2_TICK = 30;
   private static final int STRIKE_3_TICK = 50;
   private static final int FINAL_TICK = 65;
   private int alpha;

   public FirstPhaseLightningAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.playSound(this.animationLocation, XSound.ENTITY_CREEPER_PRIMED, 0.5F, 0.3F);
      ParticleEffect.SMOKE_NORMAL.display(this.centerLocation, 0.5F, 0.3F, 0.5F, 0.03F, 8, null, this.getAudience());
   }

   @Override
   public void tick() {
      this.alpha++;
      if (this.alpha % 3 == 0) {
         double angle = MathUtil.random(0.0, Math.PI * 2);
         double radius = MathUtil.random(0.5, 1.8);
         ParticleEffect.END_ROD
            .display(
               this.animationLocation.clone().add(this.sin(angle) * radius, MathUtil.random(0.2, 2.5), this.cos(angle) * radius),
               0.0F,
               0.0F,
               0.0F,
               0.02F,
               1,
               null,
               this.getAudience()
            );
      }

      if (this.alpha < 50 && this.alpha % 4 == 0) {
         double progress = this.alpha / 50.0;
         double ringRadius = 2.0 - progress * 1.2;
         double inc = Math.PI / 6;

         for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 6) {
            ParticleEffect.CRIT_MAGIC
               .display(
                  this.animationLocation.clone().add(this.sin(t) * ringRadius, 0.1, this.cos(t) * ringRadius),
                  0.0F,
                  0.02F,
                  0.0F,
                  0.01F,
                  1,
                  null,
                  this.getAudience()
               );
         }
      }

      if (this.alpha == 15) {
         this.spawnLightning(this.animationLocation.clone().add(MathUtil.random(-1.5, 1.5), 0.0, MathUtil.random(-1.5, 1.5)));
         this.playSound(this.animationLocation, XSound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.2F);
         ParticleEffect.SMOKE_LARGE.display(this.centerLocation, 0.6F, 0.2F, 0.6F, 0.05F, 8, null, this.getAudience());
      }

      if (this.alpha == 30) {
         this.spawnLightning(this.animationLocation.clone().add(MathUtil.random(-1.0, 1.0), 0.0, MathUtil.random(-1.0, 1.0)));
         this.playSound(this.animationLocation, XSound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.7F, 1.0F);
         this.playSound(this.animationLocation, XSound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4F, 1.5F);

         for (int i = 0; i < 6; i++) {
            double a = MathUtil.random(0.0, Math.PI * 2);
            ParticleEffect.END_ROD
               .display(
                  this.centerLocation.clone().add(this.sin(a) * 0.5, MathUtil.random(0.5, 2.0), this.cos(a) * 0.5),
                  0.0F,
                  0.1F,
                  0.0F,
                  0.05F,
                  1,
                  null,
                  this.getAudience()
               );
         }
      }

      if (this.alpha == 50) {
         this.spawnLightning(this.animationLocation);
         this.playSound(this.animationLocation, XSound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 0.8F);
         this.playSound(this.animationLocation, XSound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8F, 0.8F);
         ParticleEffect.EXPLOSION_LARGE.display(this.centerLocation, 0.3F, 0.5F, 0.3F, 0.0F, 3, null, this.getAudience());

         for (int i = 0; i < 15; i++) {
            ParticleEffect.END_ROD
               .display(
                  this.centerLocation.clone().add(MathUtil.random(-0.5, 0.5), MathUtil.random(0.0, 3.0), MathUtil.random(-0.5, 0.5)),
                  (float)MathUtil.random(-0.1, 0.1),
                  0.15F,
                  (float)MathUtil.random(-0.1, 0.1),
                  0.05F,
                  1,
                  null,
                  this.getAudience()
               );
         }
      }

      if (this.alpha > 50 && this.alpha % 2 == 0) {
         ParticleEffect.CRIT_MAGIC.display(this.centerLocation, 0.4F, 0.4F, 0.4F, 0.05F, 3, null, this.getAudience());
      }

      if (this.alpha >= 65) {
         this.nextPhase();
      }
   }

   private void spawnLightning(Location location) {
      FakeEntity entity = Utilities.getNMSFactory().createFakeEntity(PhoenixCrates.getLegacyInstance(), FakeEntityType.LIGHTNING, location);
      entity.prepareSpawnPacket().sendPacket(this.getAudience());
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
