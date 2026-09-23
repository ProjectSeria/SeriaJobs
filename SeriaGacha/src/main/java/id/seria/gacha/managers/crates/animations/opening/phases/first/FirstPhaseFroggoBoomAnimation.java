package id.seria.gacha.managers.crates.animations.opening.phases.first;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeFrogEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.enums.FrogVariant;
import id.seria.gacha.lib.common.nms.interfaces.entity.enums.Pose;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.texture.BlockTexture;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class FirstPhaseFroggoBoomAnimation extends PhaseRunnerImpl {
   private FakeFrogEntity frogEntity;
   private int step;
   private int delta;
   private boolean flipFlopState;
   private double alpha;
   private Vector gravity;

   public FirstPhaseFroggoBoomAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.step = 0;
      this.delta = 0;
      this.alpha = 3.0;
      this.frogEntity = Utilities.getNMSFactory()
         .createFakeEntity(PhoenixCrates.getLegacyInstance().getOriginal(), FakeEntityType.FROG, this.getAnimationLocation().clone().add(0.0, this.alpha, 0.0))
         .asFrog();
      this.frogEntity.setVariant(MathUtil.random(FrogVariant.values()));
      Services.getWatcherService().startWatching(this.frogEntity, true);
   }

   @Override
   public void tick() {
      if (this.delta % 5 == 0) {
         ParticleEffect.SPORE_BLOSSOM_AIR.display(this.frogEntity.getLocation(), 0.25F, 0.25F, 0.25F, 0.0F, 1, null, this.getAudience());
      }

      switch (this.step) {
         case 0:
            this.tickFirstStep();
            return;
         case 1:
            this.tickSecondStep();
            return;
         default:
            this.nextPhase();
      }
   }

   private void tickFirstStep() {
      Location frogLocation = this.frogEntity.getLocation();
      frogLocation.setDirection(this.player.getLocation().toVector().subtract(frogLocation.toVector()));
      if (!this.flipFlopState) {
         this.alpha -= 0.2;
         if (this.alpha < this.crate.getDisplayComponent().getDisplayHeight()) {
            this.alpha = this.crate.getDisplayComponent().getDisplayHeight();
            this.flipFlopState = true;
            this.playSound(this.centerLocation, XSound.ENTITY_GENERIC_SMALL_FALL, 0.1F, 1.0F);
            this.playSound(this.centerLocation, XSound.ENTITY_FROG_LONG_JUMP, 1.5F, 1.0F);
            this.land();
         }
      }

      if (this.delta % 2 == 0) {
         frogLocation.setX(frogLocation.getX() + 0.005);
      } else {
         frogLocation.setX(frogLocation.getX() - 0.005);
      }

      frogLocation.setY(this.getAnimationLocation().getY() + this.alpha);
      this.frogEntity.setPositionAndRotation(frogLocation);
      if (this.delta == 20) {
         this.playSound(this.centerLocation, XSound.ENTITY_FROG_AMBIENT, 1.5F, 1.0F);
         this.frogEntity.setPose(Pose.CROAKING);
      }

      if (this.player.getLocation().distance(frogLocation) <= 3.0) {
         if (this.delta == 40) {
            this.playSound(this.centerLocation, XSound.ENTITY_FROG_EAT, 1.5F, 1.0F);
            this.frogEntity.setPose(Pose.USING_TONGUE);
            ParticleEffect.CRIT.display(this.frogEntity.getLocation().clone().add(0.0, 0.1, 0.0), 0.25F, 0.25F, 0.25F, 0.0F, 5, null, this.getAudience());
         }

         if (this.delta == 42) {
            this.playSound(this.centerLocation, XSound.ENTITY_PLAYER_HURT, 0.5F, 1.0F);
            Vector knockback = this.player.getLocation().toVector().subtract(this.frogEntity.getLocation().toVector()).normalize();
            knockback = knockback.multiply(0.75);
            this.player.setVelocity(knockback);

            try {
               Method playHurtAnimation = this.player.getClass().getMethod("playHurtAnimation", float.class);
               playHurtAnimation.invoke(this.player, frogLocation.getYaw());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
               this.player.playEffect(EntityEffect.HURT);
            }
         }
      }

      this.delta++;
      if (this.delta == 60) {
         this.delta = 0;
         this.flipFlopState = false;
         this.alpha = 0.0;
         this.step++;
      }
   }

   private void land() {
      for (int i = 0; i <= 20; i++) {
         double r = MathUtil.random(0.0, Math.PI * 2);
         ParticleEffect.BLOCK_CRACK
            .display(
               this.centerLocation,
               (float)MathUtil.sin(r),
               0.5F,
               (float)MathUtil.cos(r),
               0.5F,
               0,
               new BlockTexture(this.crate.getType().getBlockType().getBukkitMaterial()),
               this.getAudience()
            );
      }
   }

   private void tickSecondStep() {
      Location frogLocation = this.frogEntity.getLocation();
      if (this.delta == 0) {
         this.openCrate();
         this.frogEntity.setPose(Pose.LONG_JUMPING);
         this.gravity = new Vector(0, 2, 0);
         this.playSound(this.centerLocation, XSound.ENTITY_FROG_LONG_JUMP, 5.0F, 1.0F);
         this.playSound(this.centerLocation, XSound.ENTITY_FROG_HURT, 5.0F, 1.0F);
      }

      if (!this.flipFlopState) {
         frogLocation.setY(frogLocation.getY() + this.gravity.getY());
         this.gravity.setY(Math.max(-1.0, this.gravity.getY() - 0.3));
      }

      if (!this.flipFlopState && frogLocation.getY() < this.getAnimationLocation().getY() - this.crate.getDisplayComponent().getDisplayHeight() - 1.0) {
         frogLocation.setY(this.getAnimationLocation().getY() + this.crate.getDisplayComponent().getDisplayHeight());
         this.frogEntity.setInvisible(true);
         this.flipFlopState = true;
         this.closeCrate();
         this.playSound(this.centerLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
         this.playSound(this.centerLocation, XSound.ENTITY_FROG_HURT, 1.0F, 1.0F);
      }

      this.frogEntity.setPositionAndRotation(frogLocation);
      if (this.delta == 35) {
         this.playSound(this.centerLocation, XSound.ENTITY_FROG_AMBIENT, 5.0F, 1.0F);
      }

      if (this.delta == 50) {
         this.step++;
      }

      this.delta++;
   }

   private void tickThirdStep() {
      this.step++;
   }

   private void tickFourthStep() {
      this.step++;
   }

   @Override
   public void unload() {
      if (this.frogEntity != null) {
         Services.getWatcherService().stopWatching(this.frogEntity);
      }
   }

   @Override
   public void nextPhase() {
      this.playSound(this.centerLocation, XSound.ENTITY_FROG_DEATH, 5.0F, 1.0F);
      this.openCrate();
      super.nextPhase();
   }
}
