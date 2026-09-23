package id.seria.gacha.managers.crates.animations.opening.phases.second;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.DustColorTransitionData;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.ColorUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class SecondPhaseRotatingHeadAnimation extends PhaseRunnerImpl {
   private static final double ARMORSTAND_SIZE = 0.74375;
   private final ItemStack skullItem = Utilities.getNMSFactory().createTexturedSkullFromUrl("https://textures.minecraft.net/texture/" + this.getTextureSkull());
   private FakeArmorStandEntity stand;
   private int step;
   private int delta;
   private int alpha;
   private double theta;

   public SecondPhaseRotatingHeadAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   private String getTextureSkull() {
      return this.properties.getPropertyOrDefault("textureSkull", "f98bc63f05f6378bf29ef10e3d82acb3ceb73a720bf80f30bc576d0ad8c40cfb");
   }

   private Color getColor() {
      return this.properties.getColorOrDefault("color", Color.WHITE);
   }

   @Override
   public void load() {
      ParticleEffect.EXPLOSION_LARGE.display(this.animationLocation, 1.0F, 1.0F, 1.0F, 0.0F, 4, null, this.getAudience());
      ParticleEffect.SMOKE_LARGE.display(this.animationLocation, 0.5F, 0.5F, 0.5F, 0.2F, 10, null, this.getAudience());
      ParticleEffect.SMOKE_NORMAL.display(this.animationLocation, 0.5F, 0.5F, 0.5F, 0.2F, 10, null, this.getAudience());
      this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 0.75F);
      this.playSound(this.animationLocation, XSound.BLOCK_DISPENSER_LAUNCH, 1.0F, 1.0F);
      this.openCrate();

      for (int i = 0; i <= 20; i++) {
         ParticleEffect.SMOKE_NORMAL
            .display(
               this.centerLocation.clone().add(MathUtil.random(-0.3, 0.3), MathUtil.random(0.0, 0.4), MathUtil.random(-0.3, 0.3)),
               0.0F,
               0.2F,
               0.0F,
               0.8F,
               0,
               null,
               this.getAudience()
            );
      }

      Location temp = this.centerLocation.clone().subtract(0.0, 0.74375, 0.0);
      this.stand = Utilities.getNMSFactory().createFakeArmorStand(PhoenixCrates.getLegacyInstance(), this.r(temp));
      this.stand.setHelmet(this.skullItem);
      this.stand.setInvisible(true);
      this.stand.setGravity(false);
      this.stand.setSmall(true);
      this.stand.setMarker(true);
      this.stand.setAudience(this.getAudience());
      Services.getWatcherService().startWatching(this.stand, false);
   }

   @Override
   public void tick() {
      ParticleEffect.DUST_COLOR_TRANSITION
         .display(
            this.stand.getLocation().clone().add(0.0, 0.69375, 0.0),
            0.1F,
            0.0F,
            0.1F,
            0.0F,
            2,
            new DustColorTransitionData(this.getColor(), ColorUtil.darkenColor(this.getColor(), 0.65F), 1.5F),
            this.getAudience()
         );
      switch (this.step) {
         case 0:
            Location temp = this.stand.getLocation().clone();
            temp.setY(this.centerLocation.getY() - 0.74375 + this.theta);
            this.stand.setHeadPose(new EulerAngle(0.0, this.delta, 0.0));
            this.theta += 0.1;
            this.delta += 5;
            if (this.alpha % 4 == 0) {
               this.playSound(this.animationLocation, XSound.ENTITY_PHANTOM_FLAP, 0.3F, 1.2F + (float)(this.theta * 0.3));
            }

            if (this.theta >= 2.0) {
               this.step++;
               this.theta = 1.0;
               this.alpha = 0;
               this.playSound(XSound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
               this.playSound(this.animationLocation, XSound.ENTITY_TURTLE_EGG_CRACK, 0.7F, 1.0F);

               for (int i = 0; i < 10; i++) {
                  ParticleEffect.SMOKE_NORMAL
                     .display(
                        temp.clone().add(0.0, 0.74375, 0.0),
                        (float)MathUtil.random(0.1, 1.0) * MathUtil.randomSign(),
                        0.0F,
                        (float)MathUtil.random(0.1, 1.0) * MathUtil.randomSign(),
                        0.6F,
                        0,
                        null,
                        this.getAudience()
                     );
               }

               return;
            }

            this.stand.setPositionAndRotation(temp);
            this.alpha++;
            break;
         case 1:
            if (this.alpha > 32) {
               ParticleEffect.EXPLOSION_LARGE
                  .display(this.stand.getLocation().clone().add(0.0, 0.74375, 0.0), 2.0F, 2.0F, 2.0F, 0.0F, 3, null, this.getAudience());
               this.playSound(this.animationLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
               this.playSound(this.animationLocation, XSound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6F, 1.0F);
               this.nextPhase();
               return;
            }

            if (this.alpha % 5 == 0 && this.theta > 0.3) {
               this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_HAT, 0.25F, 0.8F + (float)this.theta * 0.5F);
            }

            if (this.alpha % 8 == 0 && this.theta <= 0.3) {
               this.playSound(this.animationLocation, XSound.BLOCK_NOTE_BLOCK_BASS, 0.3F, 0.5F + this.alpha * 0.03F);
            }

            this.stand.setHeadPose(new EulerAngle(0.0, this.delta, 0.0));
            this.delta = (int)(this.delta - 18.0 * this.theta);
            this.theta = Math.max(0.0, this.theta - 0.01);
            this.alpha++;
      }
   }

   @Override
   public void unload() {
      Services.getWatcherService().stopWatching(this.stand);
   }
}
