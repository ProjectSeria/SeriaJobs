package id.seria.gacha.managers.crates.animations.opening.phases.second;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SecondPhaseFireAnimation extends PhaseRunnerImpl {
   private static final ItemStack FIREORB_ITEM = Utilities.getNMSFactory()
      .createTexturedSkullFromUrl("http://textures.minecraft.net/texture/c3687e25c632bce8aa61e0d64c24e694c3eea629ea944f4cf30dcfb4fbce071");
   private int delta;
   private int step;
   private double theta;
   private FakeArmorStandEntity orbStand;

   public SecondPhaseFireAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      if (this.handler.isBlockOpeningAnimationEnabled()) {
         Utilities.getNMSFactory().changeChestState(this.getAudience(), this.animationLocation, true);
      }

      this.playSound(this.animationLocation, XSound.ENTITY_BLAZE_HURT, 1.0F, 2.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_BLAZE_AMBIENT, 1.0F, 2.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_SLIME_DEATH, 1.0F, 2.0F);
      this.playSound(this.animationLocation, XSound.BLOCK_PORTAL_TRIGGER, 0.5F, 1.4F);
      this.orbStand = Utilities.getNMSFactory()
         .createFakeArmorStand(PhoenixCrates.getLegacyInstance(), this.animationLocation.clone().subtract(0.0, 1.75, 0.0));
      this.orbStand.setHelmet(FIREORB_ITEM);
      this.orbStand.setInvisible(true);
      this.orbStand.setGravity(false);
      this.orbStand.setAudience(this.getAudience());
   }

   @Override
   public void tick() {
      double inc = Math.PI / 10;

      for (int i = 0; i < 4; i++) {
         Location temp = this.animationLocation.clone().add(MathUtil.random(-0.2, 0.2), 0.5 + MathUtil.random(0.0, 0.4), MathUtil.random(-0.2, 0.2));
         ParticleEffect.SMOKE_NORMAL.display(temp, 0.0F, 0.2F, 0.0F, 0.8F, 0, null, this.getAudience());
      }

      if (this.delta % 5 == 0) {
         Location temp = this.animationLocation.clone().add(MathUtil.random(-0.2, 0.2), 0.5 + MathUtil.random(0.0, 0.4), MathUtil.random(-0.2, 0.2));
         ParticleEffect.LAVA.display(temp, 0.0F, 0.2F, 0.0F, 0.8F, 0, null, this.getAudience());
      }

      switch (this.step) {
         case 0:
            double r = 2.5 - this.theta;
            if (r < 0.2) {
               r = 0.2;
               this.nextStep();
            } else {
               for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 5) {
                  Location temp = this.animationLocation.clone().add(this.sin(t + this.theta) * r, 4.0, this.cos(t + this.theta) * r);
                  ParticleEffect.FLAME.display(temp, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
               }
            }

            this.theta += 0.05;
            break;
         case 1:
            if (this.delta == 0) {
               this.theta = 0.0;
               this.playSound(this.animationLocation, XSound.ENTITY_GHAST_HURT, 1.0F, 0.5F);
               this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 0.4F, 1.0F);
            }

            Location temp = this.animationLocation.clone().add(0.0, 4.0, 0.0);
            double y = this.theta / 3.5;
            if (y > 3.0) {
               y = 3.0;
            }

            if (y != 3.0 && this.delta % 2 == 0) {
               ParticleEffect.SMOKE_LARGE.display(temp, 0.2F, 0.1F, 0.2F, 0.0F, 1, null, this.getAudience());
               ParticleEffect.FLAME
                  .display(
                     temp,
                     (float)MathUtil.random(-0.2, 0.2),
                     (float)(-(0.5 + MathUtil.random(0.1, 0.3))),
                     (float)MathUtil.random(-0.2, 0.2),
                     0.5F,
                     0,
                     null,
                     this.getAudience()
                  );
               this.playSound(this.animationLocation, XSound.ENTITY_BLAZE_SHOOT, 0.2F, 2.0F);
            }

            if (this.delta >= 30) {
               if (this.delta == 30) {
                  Services.getWatcherService().startWatching(this.orbStand, false);
               }

               Location location = this.animationLocation.clone().add(0.0, -1.75 + y + 0.3, 0.0);
               if (y == 3.0 && this.delta % 2 == 0) {
                  this.playSound(this.animationLocation, XSound.BLOCK_LAVA_POP, 0.2F, 1.0F);
                  this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_BURP, 0.2F, 0.2F);
               }

               if (y == 3.0) {
                  this.orbStand.setHeadPose(BukkitUtil.createEulerAngleFromRad(MathUtil.random(-0.15, 0.15), this.theta, MathUtil.random(-0.15, 0.15)));
               } else {
                  this.orbStand.setHeadPose(BukkitUtil.createEulerAngleFromRad(0.0, this.theta, 0.0));
               }

               this.orbStand.setPositionAndRotation(location);
               if (this.delta % 2 == 0) {
                  Location particleLocation = location.clone().add(0.0, 1.75, 0.0);

                  for (double t = 0.0; t <= Math.PI * 2; t += Math.PI) {
                     Vector dir = new Vector(this.sin(t - this.theta), 0.0, this.cos(t - this.theta));
                     ParticleEffect.FLAME.display(particleLocation, dir, 0.3F, 0, null, this.getAudience());
                  }
               }

               if (this.delta == 120) {
                  this.nextPhase();
               }

               this.theta += 0.2;
            }
      }

      this.delta++;
   }

   @Override
   public void unload() {
      Services.getWatcherService().stopWatching(this.orbStand);
   }

   private void nextStep() {
      this.step++;
      this.delta = -1;
   }
}
