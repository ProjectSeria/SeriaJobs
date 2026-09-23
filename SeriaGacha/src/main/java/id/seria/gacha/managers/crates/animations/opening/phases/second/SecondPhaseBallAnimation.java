package id.seria.gacha.managers.crates.animations.opening.phases.second;

import id.seria.gacha.Crates;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SecondPhaseBallAnimation extends PhaseRunnerImpl {
   private static final ItemStack SKULL_ITEM = Utilities.getNMSFactory()
      .createTexturedSkullFromUrl("http://textures.minecraft.net/texture/9b2513c8d08c60ad3785d3a9a651b7329c5f26937aca2fc8dfaf3441c9bd9da2");
   private final AtomicBoolean nearbyBlocksFetched = new AtomicBoolean();
   private final List<Block> nearbyBlocks = new ArrayList<>();
   private FakeArmorStandEntity stand;
   private double ballYLimit;
   private double delta;
   private Vector velocity;

   public SecondPhaseBallAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.velocity = new Vector(0, 5, 0);
      this.ballYLimit = this.animationLocation.getY() - 1.3;
      ParticleEffect.EXPLOSION_LARGE.display(this.animationLocation, 1.0F, 1.0F, 1.0F, 0.0F, 4, null, this.getAudience());
      ParticleEffect.SMOKE_LARGE.display(this.animationLocation, 0.5F, 0.5F, 0.5F, 0.2F, 10, null, this.getAudience());
      ParticleEffect.SMOKE_NORMAL.display(this.animationLocation, 0.5F, 0.5F, 0.5F, 0.2F, 10, null, this.getAudience());
      this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_SKELETON_DEATH, 1.0F, 2.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5F, 2.0F);
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

      this.stand = Utilities.getNMSFactory()
         .createFakeArmorStand(PhoenixCrates.getLegacyInstance().getOriginal(), this.animationLocation.clone().subtract(0.0, 1.3, 0.0));
      this.stand.setHelmet(SKULL_ITEM);
      this.stand.setInvisible(true);
      this.stand.setGravity(false);
      this.stand.setAudience(this.getAudience());
      Services.getWatcherService().startWatching(this.stand, false);
      Crates.getScheduler().runSync(this.crate.getLocation(), () -> {
         Block base = this.crate.getLocation().clone().add(0.0, -1.0, 0.0).getBlock();

         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               this.nearbyBlocks.add(base.getRelative(x, 0, z));
            }
         }

         this.nearbyBlocksFetched.set(true);
      });
   }

   @Override
   public void tick() {
      if (this.nearbyBlocksFetched.get()) {
         this.velocity.multiply(0.98);
         this.velocity.add(new Vector(0.0, -0.35, 0.0));
         Location temp = this.stand.getLocation().clone();
         temp.add(this.velocity.clone().multiply(0.3));
         if (this.stand.getLocation().getY() < this.ballYLimit) {
            temp.setY(this.ballYLimit);
            this.velocity.setY(-this.velocity.getY());
            this.makeParticlesOnGround();
            this.playSound(this.animationLocation, XSound.ENTITY_SLIME_SQUISH_SMALL, 0.8F, 1.2F);
            this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_SMALL_FALL, 0.4F, 0.5F);
            this.playSound(this.animationLocation, XSound.BLOCK_STONE_STEP, 1.0F, 1.0F);
         }

         this.stand.setPositionAndRotation(temp);
         this.stand
            .setHeadPose(
               BukkitUtil.createEulerAngleFromRad(
                  0.0, Math.toRadians(this.stand.getHeadPose().getY()) + 0.25 + Math.abs(this.velocity.getY() * 0.1), this.velocity.getY() * 0.05
               )
            );
         ParticleEffect.REDSTONE
            .display(this.stand.getLocation().clone().add(0.0, 2.0, 0.0), 0.5F, 0.5F, 0.5F, 0.0F, 2, RegularColor.random(), this.getAudience());
         if (this.delta == 78.0) {
            this.nextPhase();
         }

         this.delta++;
      }
   }

   @Override
   public void unload() {
      Services.getWatcherService().stopWatching(this.stand);
   }

   private void makeParticlesOnGround() {
      this.nearbyBlocks.forEach(block -> {
         Material type = block.getType();
         if (type != null && type != Material.AIR) {
            ParticleEffect.BLOCK_CRACK.display(block.getLocation().add(0.5, 1.0, 0.5), 0.5F, 0.0F, 0.5F, 0.3F, 3, new BlockTexture(type), this.getAudience());
         }
      });
   }
}
