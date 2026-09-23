package id.seria.gacha.managers.crates.animations.opening.phases.third;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.internal.RewardsDisplay;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ThirdPhaseSmokeSpiralAnimation extends PhaseRunnerImpl {
   private final RewardsDisplay rewardsDisplay;
   private final RegularColor particleColor = new RegularColor(this.getColor());
   private Location baseLocation;
   private int alpha;
   private double delta;
   private double theta;

   public ThirdPhaseSmokeSpiralAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
      this.rewardsDisplay = new RewardsDisplay(
         this, this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()))
      );
   }

   public Color getColor() {
      return this.properties.getColorOrDefault("color", Color.WHITE);
   }

   @Override
   public void load() {
      this.baseLocation = this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()));
      ParticleEffect.SMOKE_NORMAL.display(this.baseLocation, 0.2F, 0.2F, 0.2F, 0.4F, 20, null, this.getAudience());
      ParticleEffect.EXPLOSION_NORMAL.display(this.baseLocation, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
      ParticleEffect.SPELL_MOB_AMBIENT.display(this.baseLocation, 0.0F, 0.0F, 0.0F, 0.5F, 10, new RegularColor(Color.DARK_GRAY), this.getAudience());
      ParticleEffect.EXPLOSION_LARGE.display(this.baseLocation, this.getAudience());
      this.playSound(this.animationLocation, XSound.ENTITY_ENDER_DRAGON_GROWL, 0.3F, 1.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
      List<? extends BaseReward> rewards = this.handler.generateRewards();
      this.rewardsDisplay.create(rewards);
      this.session.giveRewards(rewards);
   }

   @Override
   public void tick() {
      for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / 2) {
         Vector relative = new Vector(this.sin(this.delta / 2.0 + t), 0.0, this.cos(this.delta / 2.0 + t));
         ParticleEffect.SMOKE_NORMAL.display(this.animationLocation.clone().add(relative), relative, 0.1F, 0, null, this.getAudience());
      }

      ParticleEffect.SPELL_MOB_AMBIENT
         .display(
            this.baseLocation.clone().add(this.sin(this.delta), this.sin(this.delta / 2.0) / 2.0, this.cos(this.delta)),
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            1,
            this.particleColor,
            this.getAudience()
         );
      if (this.alpha % 10 == 0) {
         this.playSound(this.animationLocation, XSound.ENTITY_ENDER_DRAGON_FLAP, 0.3F, 1.0F);
      }

      if (this.alpha == 70) {
         this.nextPhase();
      }

      this.alpha++;
      this.delta += 0.3;
   }

   @Override
   public void unload() {
      this.rewardsDisplay.destroy();
      this.closeCrate();
      this.playSound(this.animationLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 0.2F);
   }
}
