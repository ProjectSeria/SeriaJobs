package id.seria.gacha.managers.crates.animations.opening.phases.second;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeItemEntity;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SecondPhaseCreeperAnimation extends PhaseRunnerImpl {
   private static final int RISE_TICKS = 25;
   private static final int FIRST_HIT_TICK = 35;
   private static final int SECOND_HIT_TICK = 48;
   private static final int THIRD_HIT_TICK = 58;
   private static final int LIGHTNING_TICK = 70;
   private static final int EXPLODE_TICK = 70;
   private static final int DISPLAY_TICKS = 65;
   private static final int PINATA_ITEMS = 10;
   private static final byte ENTITY_HURT_EVENT = 2;
   private FakeEntity creeperEntity;
   private final List<FakeItemEntity> droppedItems = new ArrayList<>();
   private Location creeperRestLocation;
   private SecondPhaseCreeperAnimation.Phase phase;
   private int tickCounter;
   private double riseStartY;
   private int hurtKnockbackRemaining;

   public SecondPhaseCreeperAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.openCrate();
      this.riseStartY = this.animationLocation.getY() - 1.5;
      Location spawnLoc = this.animationLocation.clone();
      spawnLoc.setY(this.riseStartY);
      this.creeperEntity = Utilities.getNMSFactory().createFakeEntity(PhoenixCrates.getLegacyInstance(), FakeEntityType.CREEPER, spawnLoc);
      this.creeperEntity.setGravity(false);
      this.creeperEntity.setSilent(true);
      this.creeperEntity.setAudience(this.getAudience());
      Services.getWatcherService().startWatching(this.creeperEntity, false);
      this.phase = SecondPhaseCreeperAnimation.Phase.RISING;
      this.tickCounter = 0;
      this.hurtKnockbackRemaining = 0;
      this.playSound(this.animationLocation, XSound.ENTITY_CREEPER_PRIMED, 0.7F, 0.6F);
   }

   @Override
   public void tick() {
      this.tickCounter++;
      if (this.hurtKnockbackRemaining > 0) {
         this.hurtKnockbackRemaining--;
         if (this.hurtKnockbackRemaining == 0 && this.creeperEntity != null && this.creeperRestLocation != null) {
            this.creeperEntity.setPositionAndRotation(this.creeperRestLocation);
         }
      }

      switch (this.phase) {
         case RISING:
            this.tickRising();
            break;
         case HURTING:
            this.tickHurting();
            break;
         case LIGHTNING:
            this.tickLightning();
            break;
         case EXPLODING:
            this.tickExploding();
            break;
         case DISPLAY:
            this.tickDisplay();
      }
   }

   private void tickRising() {
      if (this.creeperEntity != null && this.tickCounter <= 25) {
         double progress = this.tickCounter / 25.0;
         double easedProgress = 1.0 - Math.pow(1.0 - progress, 3.0);
         double y = this.riseStartY + easedProgress * 1.5;
         Location loc = this.creeperEntity.getLocation().clone();
         loc.setY(y);
         this.creeperEntity.setPositionAndRotation(loc);
      }

      if (this.tickCounter % 3 == 0) {
         ParticleEffect.SMOKE_NORMAL.display(this.animationLocation, 0.3F, 0.1F, 0.3F, 0.02F, 3, null, this.getAudience());
      }

      if (this.tickCounter >= 25) {
         this.creeperRestLocation = this.creeperEntity != null ? this.creeperEntity.getLocation().clone() : this.animationLocation.clone();
         this.phase = SecondPhaseCreeperAnimation.Phase.HURTING;
         this.playSound(this.animationLocation, XSound.ENTITY_CREEPER_PRIMED, 0.5F, 1.0F);
      }
   }

   private void tickHurting() {
      if (this.tickCounter == 35 || this.tickCounter == 48 || this.tickCounter == 58) {
         this.performHurt();
      }

      if (this.tickCounter >= 66) {
         this.phase = SecondPhaseCreeperAnimation.Phase.LIGHTNING;
      }
   }

   private void performHurt() {
      if (this.creeperEntity != null) {
         this.creeperEntity.prepareEventPacket((byte)2).sendPacket(this.getAudience());
         Location knockedLoc = this.creeperRestLocation.clone().add(MathUtil.random(-0.15, 0.15), 0.08, MathUtil.random(-0.15, 0.15));
         this.creeperEntity.setPositionAndRotation(knockedLoc);
         this.hurtKnockbackRemaining = 3;
         this.playSound(this.animationLocation, XSound.ENTITY_CREEPER_HURT, 1.0F, 1.0F);
         this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_ATTACK_STRONG, 0.6F, 0.7F);
         Location particleLoc = this.creeperRestLocation.clone().add(0.0, 1.0, 0.0);
         ParticleEffect.SMOKE_NORMAL.display(particleLoc, 0.3F, 0.4F, 0.3F, 0.05F, 8, null, this.getAudience());
         ParticleEffect.CRIT.display(particleLoc, 0.3F, 0.3F, 0.3F, 0.2F, 10, null, this.getAudience());
         if (this.tickCounter == 58) {
            this.playSound(this.animationLocation, XSound.ENTITY_CREEPER_HURT, 0.8F, 0.6F);
         }
      }
   }

   private void tickLightning() {
      if (this.tickCounter >= 70) {
         FakeEntity lightning = Utilities.getNMSFactory().createFakeEntity(PhoenixCrates.getLegacyInstance(), FakeEntityType.LIGHTNING, this.animationLocation);
         lightning.prepareSpawnPacket().sendPacket(this.getAudience());
         this.playSound(this.animationLocation, XSound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 1.0F);
         this.playSound(this.animationLocation, XSound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8F, 0.8F);
         this.explode();
      }
   }

   private void explode() {
      this.phase = SecondPhaseCreeperAnimation.Phase.EXPLODING;
      if (this.creeperEntity != null) {
         Services.getWatcherService().stopWatching(this.creeperEntity);
      }

      this.creeperEntity = null;
      Location center = this.animationLocation.clone().add(0.0, 0.5, 0.0);
      ParticleEffect.EXPLOSION_LARGE.display(center, 0.5F, 0.5F, 0.5F, 0.0F, 8, null, this.getAudience());
      ParticleEffect.EXPLOSION_NORMAL.display(center, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
      ParticleEffect.SMOKE_LARGE.display(center, 0.6F, 0.4F, 0.6F, 0.12F, 25, null, this.getAudience());
      ParticleEffect.FLAME.display(center, 0.5F, 0.3F, 0.5F, 0.08F, 15, null, this.getAudience());
      this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.8F);
      this.playSound(this.animationLocation, XSound.ENTITY_CREEPER_DEATH, 0.7F, 1.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.4F, 0.6F);
      this.spawnPinataItems();
   }

   private void spawnPinataItems() {
      List<? extends BaseReward> rewards = this.handler.generateRewards();
      if (rewards != null && !rewards.isEmpty()) {
         BaseReward wonReward = rewards.get(0);
         ItemStack rewardItem = wonReward.getDisplayItem().build();
         if (rewardItem != null && rewardItem.getType() != Material.AIR) {
            Location base = this.animationLocation.clone().add(0.0, 0.5, 0.0);

            for (int i = 0; i < 10; i++) {
               double angle = MathUtil.random(0.0, Math.PI * 2);
               double spread = MathUtil.random(0.05, 0.15);
               Vector direction = new Vector(Math.cos(angle) * spread, MathUtil.random(0.2, 0.4), Math.sin(angle) * spread);
               FakeItemEntity entity = Utilities.getNMSFactory().createFakeItem(PhoenixCrates.getLegacyInstance().getOriginal(), base);
               entity.setItemStack(new ItemStack(rewardItem.getType()));
               entity.setVelocity(direction);
               entity.setAudience(this.getAudience());
               Services.getWatcherService().startWatching(entity, false);
               this.droppedItems.add(entity);
            }

            this.session.giveRewards(rewards);
            this.playSound(this.animationLocation, XSound.ENTITY_ITEM_PICKUP, 0.5F, 1.2F);
         } else {
            this.session.giveRewards(rewards);
         }
      }
   }

   private void tickExploding() {
      this.phase = SecondPhaseCreeperAnimation.Phase.DISPLAY;
   }

   private void tickDisplay() {
      if (this.tickCounter >= 135) {
         this.nextPhase();
      }
   }

   @Override
   public void unload() {
      if (this.creeperEntity != null) {
         Services.getWatcherService().stopWatching(this.creeperEntity);
      }

      this.creeperEntity = null;

      for (FakeItemEntity entity : this.droppedItems) {
         Services.getWatcherService().stopWatching(entity);
      }

      this.droppedItems.clear();
      this.closeCrate();
      this.playSound(this.animationLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 0.2F);
   }

   private enum Phase {
      RISING,
      HURTING,
      LIGHTNING,
      EXPLODING,
      DISPLAY;

      Phase() {
      }
   }
}
