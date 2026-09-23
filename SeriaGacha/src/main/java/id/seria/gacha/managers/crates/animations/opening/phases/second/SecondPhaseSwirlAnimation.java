package id.seria.gacha.managers.crates.animations.opening.phases.second;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.texture.BlockTexture;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import org.bukkit.Location;

public class SecondPhaseSwirlAnimation extends PhaseRunnerImpl {
   private final RegularColor color = new RegularColor(this.properties.getColorOrDefault("color", Color.WHITE));
   private double delta;

   public SecondPhaseSwirlAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.playSound(this.animationLocation, XSound.BLOCK_LAVA_POP, 2.0F, 1.0F);
      this.playSound(this.animationLocation, XSound.BLOCK_LAVA_EXTINGUISH, 2.0F, 1.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.1F);
      this.playSound(this.animationLocation, XSound.BLOCK_FIRE_AMBIENT, 1.0F, 0.7F);
      this.playSound(this.animationLocation, XSound.ENTITY_ENDERMAN_AMBIENT, 1.0F, 2.0F);
      ParticleEffect.EXPLOSION_LARGE.display(this.animationLocation.clone().add(0.0, 1.0, 0.0), this.getAudience());

      for (int i = 0; i <= 10; i++) {
         ParticleEffect.BLOCK_CRACK
            .display(
               this.animationLocation.clone().add(0.0, 0.5, 0.0),
               0.6F,
               0.6F,
               0.6F,
               0.3F,
               20,
               new BlockTexture(this.crate.getType().getBlockType().getBukkitMaterial()),
               this.getAudience()
            );
      }
   }

   @Override
   public void tick() {
      double diff = 2.0 - this.delta / 2.4;
      if (diff < 0.0) {
         diff = 0.0;
      }

      for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 2) {
         ParticleEffect.REDSTONE
            .display(
               this.animationLocation.clone().add(this.sin(this.delta * 2.0 + t) * diff, 3.5 - this.delta / 10.0, this.cos(this.delta * 2.0 + t) * diff),
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               1,
               this.color,
               this.getAudience()
            );
      }

      for (int i = 0; i <= 4; i++) {
         ParticleEffect.SMOKE_NORMAL
            .display(
               this.animationLocation.clone().add(MathUtil.random(-0.2, 0.2), 0.5 + MathUtil.random(0.0, 0.4), MathUtil.random(-0.2, 0.2)),
               0.0F,
               0.2F,
               0.0F,
               0.8F,
               0,
               null,
               this.getAudience()
            );
      }

      if (this.delta > 3.5) {
         Location temp = this.animationLocation.clone().add(0.0, 3.0, 0.0);
         ParticleEffect.SMOKE_NORMAL.display(temp, 0.2F, 0.2F, 0.2F, 0.4F, 20, null, this.getAudience());
         ParticleEffect.EXPLOSION_NORMAL.display(temp, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
         ParticleEffect.SPELL_MOB_AMBIENT.display(temp, 0.0F, 0.0F, 0.0F, 0.5F, 10, this.color, this.getAudience());
         ParticleEffect.EXPLOSION_LARGE.display(temp, this.getAudience());
         this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
         this.nextPhase();
      }

      this.delta += 0.1;
   }

   @Override
   public void unload() {
   }
}
