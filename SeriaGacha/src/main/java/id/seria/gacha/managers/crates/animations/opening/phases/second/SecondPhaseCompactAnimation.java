package id.seria.gacha.managers.crates.animations.opening.phases.second;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.texture.BlockTexture;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SecondPhaseCompactAnimation extends PhaseRunnerImpl {
   private FakeArmorStandEntity stand;
   private int delta;
   private double alpha;
   private double theta = Math.PI * 4;

   public SecondPhaseCompactAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.playSound(this.centerLocation, XSound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.5F);
      this.playSound(this.centerLocation, XSound.ENTITY_SKELETON_DEATH, 1.0F, 2.0F);
      this.playSound(this.centerLocation, XSound.BLOCK_WOOD_BREAK, 1.0F, 1.0F);
      ParticleEffect.EXPLOSION_LARGE.display(this.centerLocation, 0.5F, 0.5F, 0.5F, 0.0F, 2, null, this.getAudience());
      ParticleEffect.SMOKE_LARGE.display(this.centerLocation, 0.25F, 0.0F, 0.25F, 0.2F, 7, null, this.getAudience());
      ParticleEffect.BLOCK_CRACK
         .display(
            this.centerLocation, 0.5F, 0.5F, 0.5F, 0.3F, 20, new BlockTexture(this.crate.getType().getBlockType().getBukkitMaterial()), this.getAudience()
         );

      for (int i = 0; i < 20; i++) {
         Vector v = new Vector(MathUtil.random(-2.0, 2.0), MathUtil.random(-2.0, 2.0), MathUtil.random(-2.0, 2.0));
         Location loc = this.centerLocation.clone().add(v);
         v.multiply(-1);
         ParticleEffect.SMOKE_NORMAL.display(loc, v.multiply(0.5), 0.4F, 0, null, this.getAudience());
      }

      Location temp = this.centerLocation.clone().subtract(0.0, 1.75, 0.0);
      this.stand = Utilities.getNMSFactory().createFakeArmorStand(PhoenixCrates.getLegacyInstance(), this.r(temp));
      this.stand.setHelmet(CrateSkullFetcher.fetchSkull(this.crate.getType().getBlockType()));
      this.stand.setInvisible(true);
      this.stand.setAudience(this.getAudience());
      this.stand.setGravity(false);
      Services.getWatcherService().startWatching(this.stand, false);
      this.handler.hideCrate();
   }

   @Override
   public void tick() {
      this.delta++;
      if (this.delta >= 10) {
         if (this.delta == 10) {
            this.playSound(this.centerLocation, XSound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 2.0F);
            this.playSound(this.centerLocation, XSound.ENTITY_SLIME_DEATH, 0.5F, 0.1F);
            this.playSound(this.centerLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 2.0F);
         }

         this.stand.setHeadPose(BukkitUtil.createEulerAngleFromRad(0.0, this.theta, 0.0));
         double y = this.alpha;
         if (y > 2.5) {
            y = 2.5;
         }

         y -= 1.75;
         Location loc = this.centerLocation.clone();
         loc.setY(this.centerLocation.getY() + y + this.sin(this.alpha));
         this.stand.setPositionAndRotation(this.r(loc));
         ParticleEffect.REDSTONE.display(this.getHeadLocation(), 0.5F, 0.5F, 0.5F, 0.1F, 1, new RegularColor(this.getColor()), this.getAudience());
         this.alpha += 0.085;
         this.theta = this.theta - MathUtil.clamp(this.theta * 0.05, 0.1, 0.2);
         if (this.delta % 6 == 0 && this.theta > 2.0) {
            this.playSound(this.centerLocation, XSound.ENTITY_PHANTOM_FLAP, 0.2F, 1.5F + (float)(this.alpha * 0.1));
         }

         if (this.delta % 10 == 0 && this.theta <= 2.0 && this.theta > 0.0) {
            this.playSound(this.centerLocation, XSound.BLOCK_NOTE_BLOCK_HAT, 0.2F, 1.0F + (float)Math.abs(this.theta) * 0.3F);
         }

         if (this.theta <= -0.25) {
            this.playSound(this.centerLocation, XSound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
            this.playSound(this.centerLocation, XSound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.2F);
            this.nextPhase();
         }
      }
   }

   private Color getColor() {
      return this.properties.getColorOrDefault("color", Color.WHITE);
   }

   private Location getHeadLocation() {
      return this.stand.getLocation().add(0.0, 1.75, 0.0);
   }

   @Override
   public void unload() {
      Services.getWatcherService().stopWatching(this.stand);
   }
}
