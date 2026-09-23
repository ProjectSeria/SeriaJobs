package id.seria.gacha.managers.crates.animations.opening.phases.first;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.awt.Color;
import org.bukkit.util.Vector;

public class FirstPhaseRainbowAnimation extends PhaseRunnerImpl {
   private static Color[] RAINBOW_COLORS = new Color[]{
      new Color(143, 0, 255), new Color(75, 0, 130), Color.BLUE, Color.GREEN, Color.YELLOW, new Color(255, 115, 54), Color.RED
   };
   private int delta;
   private double alpha;

   public FirstPhaseRainbowAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      this.alpha = 0.0;
   }

   @Override
   public void tick() {
      double p = 3.241592653589793;
      if (this.delta++ % 2 == 0) {
         this.playSound(this.getAnimationLocation(), XSound.BLOCK_NOTE_BLOCK_HARP, 2.0F, (float)((this.alpha + 0.2) / 2.269114858555936));
      }

      for (int l = 0; l < 3; l++) {
         for (int i = 0; i < 14; i++) {
            ParticleEffect.REDSTONE
               .display(
                  this.getAnimationLocation()
                     .clone()
                     .add(
                        this.r(
                           new Vector(
                              MathUtil.sin(this.alpha - (Math.PI / 2)) * (1.5 + i * 0.15), MathUtil.cos(this.alpha - (Math.PI / 2)) * (1.5 + i * 0.15), 0.0
                           )
                        )
                     ),
                  0.0F,
                  0.0F,
                  0.0F,
                  0.0F,
                  1,
                  new RegularColor(RAINBOW_COLORS[i / 2]),
                  this.getAudience()
               );
         }

         this.alpha += 0.05;
      }

      if (this.alpha > p) {
         this.nextPhase();
      }
   }

   @Override
   public void unload() {
   }

   @Override
   public void nextPhase() {
      this.openCrate();
      super.nextPhase();
   }
}
