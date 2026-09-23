package id.seria.gacha.managers.crates.animations.opening.phases.third;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.internal.RewardsDisplay;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import java.util.List;
import org.bukkit.Location;

public class ThirdPhaseDefaultAnimation extends PhaseRunnerImpl {
   private static final int DISPLAY_TICKS = 70;
   private final RewardsDisplay rewardsDisplay;
   private final RegularColor color;
   private final Color rawColor = this.properties.getColorOrDefault("color", Color.WHITE);
   private int alpha;
   private double delta;

   public ThirdPhaseDefaultAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
      this.color = new RegularColor(this.rawColor);
      this.rewardsDisplay = new RewardsDisplay(
         this, this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()))
      );
   }

   @Override
   public void load() {
      Location baseLocation = this.animationLocation.clone().add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()));
      ParticleEffect.SMOKE_NORMAL.display(baseLocation, 0.2F, 0.2F, 0.2F, 0.4F, 20, null, this.getAudience());
      ParticleEffect.EXPLOSION_NORMAL.display(baseLocation, 0.0F, 0.0F, 0.0F, 0.0F, 1, null, this.getAudience());
      ParticleEffect.SPELL_MOB_AMBIENT.display(baseLocation, 0.0F, 0.0F, 0.0F, 0.5F, 10, new RegularColor(Color.DARK_GRAY), this.getAudience());
      ParticleEffect.EXPLOSION_LARGE.display(baseLocation, this.getAudience());
      this.playSound(this.animationLocation, XSound.ENTITY_ENDER_DRAGON_GROWL, 0.3F, 1.0F);
      this.playSound(this.animationLocation, XSound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
      List<? extends BaseReward> rewards = this.handler.generateRewards();
      this.rewardsDisplay.create(rewards);
      this.session.giveRewards(rewards);
   }

   @Override
   public void tick() {
      if (!this.handler.isHiddenCrate()) {
         double groundAngle1 = this.delta * 1.5;
         double groundAngle2 = groundAngle1 + Math.PI;
         double groundRadius = 0.7;
         ParticleEffect.REDSTONE
            .display(
               this.animationLocation.clone().add(this.sin(groundAngle1) * 0.7, 0.05, this.cos(groundAngle1) * 0.7),
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               1,
               this.color,
               this.getAudience()
            );
         ParticleEffect.REDSTONE
            .display(
               this.animationLocation.clone().add(this.sin(groundAngle2) * 0.7, 0.05, this.cos(groundAngle2) * 0.7),
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               1,
               this.color,
               this.getAudience()
            );
      }

      ParticleEffect.SPELL_MOB_AMBIENT
         .display(
            this.animationLocation.clone().add(this.sin(this.delta) * 0.8, 2.5 + this.sin(this.delta * 0.5) * 0.4, this.cos(this.delta) * 0.8),
            0.0F,
            0.0F,
            0.0F,
            0.5F,
            1,
            this.color,
            this.getAudience()
         );
      ParticleEffect.SPELL_MOB_AMBIENT
         .display(
            this.animationLocation.clone().add(this.sin(-this.delta * 0.7) * 1.0, 1.8 + this.sin(this.delta * 0.3) * 0.3, this.cos(-this.delta * 0.7) * 1.0),
            0.0F,
            0.0F,
            0.0F,
            0.5F,
            1,
            new RegularColor(this.brighten(this.rawColor, 0.4F)),
            this.getAudience()
         );
      if (this.alpha % 4 == 0) {
         ParticleEffect.END_ROD
            .display(
               this.animationLocation.clone().add(MathUtil.random(-0.5, 0.5), MathUtil.random(0.3, 1.5), MathUtil.random(-0.5, 0.5)),
               0.0F,
               0.03F,
               0.0F,
               0.01F,
               1,
               null,
               this.getAudience()
            );
      }

      if (this.alpha % 8 == 0) {
         ParticleEffect.VILLAGER_HAPPY
            .display(
               this.animationLocation.clone().add(MathUtil.random(-0.6, 0.6), MathUtil.random(2.0, 3.0), MathUtil.random(-0.6, 0.6)),
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               1,
               null,
               this.getAudience()
            );
      }

      if (this.alpha % 10 == 0) {
         this.playSound(this.animationLocation, XSound.ENTITY_ENDER_DRAGON_FLAP, 0.3F, 1.0F);
      }

      if (this.alpha >= 70) {
         this.nextPhase();
      }

      this.alpha++;
      this.delta += 0.25;
   }

   private Color brighten(Color base, float amount) {
      return new Color(
         Math.min(255, (int)(base.getRed() + (255 - base.getRed()) * amount)),
         Math.min(255, (int)(base.getGreen() + (255 - base.getGreen()) * amount)),
         Math.min(255, (int)(base.getBlue() + (255 - base.getBlue()) * amount))
      );
   }

   @Override
   public void unload() {
      this.rewardsDisplay.destroy();
      this.closeCrate();
      this.playSound(this.animationLocation, XSound.ENTITY_CHICKEN_EGG, 1.0F, 0.2F);
   }
}
