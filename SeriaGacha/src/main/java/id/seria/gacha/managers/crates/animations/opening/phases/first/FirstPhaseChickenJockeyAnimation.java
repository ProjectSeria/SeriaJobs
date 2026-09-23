package id.seria.gacha.managers.crates.animations.opening.phases.first;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
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
import org.bukkit.entity.Zombie;
import org.bukkit.util.Vector;

public class FirstPhaseChickenJockeyAnimation extends PhaseRunnerImpl {
   private FakeArmorStandEntity standEntity;
   private FakeLivingEntity chickenEntity;
   private FakeLivingEntity zombieEntity;
   private int step;
   private int delta;
   private boolean flipFlopState;
   private double alpha;
   private boolean flipflop;

   public FirstPhaseChickenJockeyAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.step = 0;
      this.delta = 0;
      this.chickenEntity = Utilities.getNMSFactory()
         .createFakeEntity(
            PhoenixCrates.getLegacyInstance().getOriginal(),
            FakeEntityType.CHICKEN,
            this.getAnimationLocation().clone().add(0.0, this.getCrate().getDisplayComponent().getDisplayHeight() + 0.5, 0.0)
         )
         .asLivingEntity();
      this.standEntity = Utilities.getNMSFactory()
         .createFakeEntity(PhoenixCrates.getLegacyInstance().getOriginal(), FakeEntityType.ARMOR_STAND, this.getAnimationLocation().clone().add(0.0, 2.5, 0.0))
         .asArmorStand();
      this.standEntity.setInvisible(true);
      this.zombieEntity = Utilities.getNMSFactory()
         .createFakeEntity(PhoenixCrates.getLegacyInstance().getOriginal(), FakeEntityType.ZOMBIE, this.getAnimationLocation().clone().add(0.0, -10.0, 0.0))
         .asLivingEntity();
      ((Zombie)this.zombieEntity.getBukkitEntity()).setBaby(true);
      Services.getWatcherService().startWatching(this.chickenEntity, true);
      Services.getWatcherService().startWatching(this.standEntity, true);
      this.playSound(this.centerLocation, XSound.ENTITY_CHICKEN_EGG, 1.5F, 1.0F);
      this.playSound(this.centerLocation, XSound.ENTITY_CHICKEN_AMBIENT, 1.5F, 1.0F);
   }

   @Override
   public void tick() {
      Location chickenLocation = this.chickenEntity.getLocation();
      Location playerLocation = this.player.getLocation().add(0.0, this.player.getEyeHeight(false) - 1.0, 0.0);
      chickenLocation.setDirection(playerLocation.toVector().subtract(chickenLocation.toVector()));
      this.chickenEntity.setHeadYaw(chickenLocation.getYaw());
      this.chickenEntity.setBodyYaw(chickenLocation.getYaw());
      this.chickenEntity.setHeadPitch(chickenLocation.getPitch());
      this.chickenEntity.setPositionChanged(true);
      this.zombieEntity.setHeadYaw(chickenLocation.getYaw());
      this.zombieEntity.setBodyYaw(chickenLocation.getYaw());
      this.zombieEntity.setHeadPitch(chickenLocation.getPitch());
      this.zombieEntity.setPositionChanged(true);
      switch (this.step) {
         case 0:
            this.tickFirstStep();
            return;
         case 1:
            this.tickSecondStep();
            return;
         case 2:
            this.tickThirdStep();
            return;
         case 3:
            this.tickFourthStep();
            return;
         default:
            this.nextPhase();
      }
   }

   private void tickFirstStep() {
      this.delta++;
      if (!this.flipFlopState) {
         Location chickenLocation = this.chickenEntity.getLocation();
         chickenLocation.setY(chickenLocation.getY() - 0.05);
         double targetY = this.getAnimationLocation().getY() + this.getCrate().getDisplayComponent().getDisplayHeight();
         if (chickenLocation.getY() < targetY) {
            chickenLocation.setY(targetY);
            this.chickenEntity.setOnGround(true);
            this.playSound(this.centerLocation, XSound.ENTITY_CHICKEN_STEP, 1.5F, 0.5F);
            this.makeCrackParticles(0.5F, 5);
            this.flipFlopState = true;
         }

         this.chickenEntity.setPositionAndRotation(chickenLocation);
      }

      if (this.delta == 20) {
         Services.getWatcherService().startWatching(this.zombieEntity, true);
         this.standEntity.setPassenger(this.zombieEntity);
         this.delta = 0;
         this.flipFlopState = false;
         this.step++;
      }
   }

   private void tickSecondStep() {
      this.delta++;
      if (!this.flipFlopState) {
         Location standLocation = this.standEntity.getLocation();
         standLocation.setY(standLocation.getY() - 0.3);
         double targetY = this.getAnimationLocation().getY() + this.getCrate().getDisplayComponent().getDisplayHeight() - 1.3;
         if (standLocation.getY() < targetY) {
            standLocation.setY(targetY);
            this.makeCrackParticles(0.5F, 20);
            this.playSound(this.centerLocation, XSound.ENTITY_CHICKEN_HURT, 1.5F, 1.5F);
            this.playSound(this.centerLocation, XSound.ENTITY_ITEM_FRAME_ADD_ITEM, 1.0F, 1.0F);
            this.playSound(this.centerLocation, XSound.ENTITY_HORSE_SADDLE, 1.5F, 1.0F);
            this.flipFlopState = true;
         }

         this.standEntity.setPositionAndRotation(standLocation);
      }

      Location zombieLocation = this.standEntity.getLocation().clone().add(0.0, 2.5, 0.0);
      Location playerLocation = this.player.getLocation().add(0.0, this.player.getEyeHeight(false), 0.0);
      zombieLocation.setDirection(playerLocation.toVector().subtract(zombieLocation.toVector()));
      this.zombieEntity.setHeadYaw(zombieLocation.getYaw());
      this.zombieEntity.setHeadPitch(zombieLocation.getPitch());
      this.zombieEntity.setBodyYaw(zombieLocation.getYaw());
      if (this.delta == 10) {
         this.chickenEntity.setOnGround(false);
      }

      if (this.delta == 15) {
         this.chickenEntity.setOnGround(true);
      }

      if (this.delta == 30) {
         this.delta = 0;
         this.flipFlopState = false;
         this.step++;
      }
   }

   private void tickThirdStep() {
      this.delta++;
      if (this.delta == 1) {
         this.chickenEntity.setOnGround(false);
      }

      if (this.delta % 2 == 0) {
         this.playSound(this.centerLocation, XSound.ENTITY_CHICKEN_STEP, 0.1F, 1.0F);
         this.makeCrackParticles(0.5F, 20);
      }

      if (this.delta % 5 == 0) {
         this.playSound(this.centerLocation, XSound.ENTITY_CHICKEN_AMBIENT, 1.0F, 1.0F);
      }

      if (this.delta == 40) {
         this.playSound(this.centerLocation, XSound.ENTITY_ZOMBIE_AMBIENT, 1.0F, 1.5F);
         this.playSound(this.centerLocation, XSound.ENTITY_ZOMBIE_HURT, 1.0F, 1.5F);
         this.delta = 0;
         this.flipFlopState = false;
         this.step++;
      }
   }

   private void tickFourthStep() {
      this.delta++;
      Location chickenLocation = this.chickenEntity.getLocation();
      Location zombieLocation = this.standEntity.getLocation();
      Location playerLocation = this.player.getLocation().add(0.0, this.player.getEyeHeight(false), 0.0);
      Vector dash = playerLocation.toVector().subtract(this.animationLocation.clone().add(0.0, 1.0, 0.0).toVector()).normalize();
      dash.setY(-0.05);
      dash.multiply(1);
      chickenLocation.add(dash);
      zombieLocation.add(dash);
      this.chickenEntity.setPositionAndRotation(chickenLocation);
      this.standEntity.setPositionAndRotation(zombieLocation);
      chickenLocation.setY(0.0);
      playerLocation.setY(0.0);
      if (this.delta > 5) {
         this.playSound(this.centerLocation, XSound.ENTITY_PLAYER_HURT, 0.5F, 1.0F);
         Vector knockback = this.player.getLocation().toVector().subtract(this.animationLocation.clone().add(0.0, 1.0, 0.0).toVector()).normalize();
         this.player.setVelocity(knockback.multiply(0.75));

         try {
            Method playHurtAnimation = this.player.getClass().getMethod("playHurtAnimation", float.class);
            playHurtAnimation.invoke(this.player, chickenLocation.getYaw());
         } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            this.player.playEffect(EntityEffect.HURT);
         }

         this.step++;
      }

      if (this.delta == 10) {
         this.step++;
      }
   }

   @Override
   public void unload() {
      if (this.chickenEntity != null) {
         Services.getWatcherService().stopWatching(this.chickenEntity);
      }

      if (this.standEntity != null) {
         Services.getWatcherService().stopWatching(this.standEntity);
      }

      if (this.zombieEntity != null) {
         Services.getWatcherService().stopWatching(this.zombieEntity);
      }
   }

   @Override
   public void nextPhase() {
      this.openCrate();
      super.nextPhase();
   }

   private void makeCrackParticles(float offset, int amountParticles) {
      for (int i = 0; i <= amountParticles; i++) {
         double r = MathUtil.random(0.0, Math.PI * 2);
         ParticleEffect.BLOCK_CRACK
            .display(
               this.centerLocation.clone().add(0.0, offset, 0.0),
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
}
