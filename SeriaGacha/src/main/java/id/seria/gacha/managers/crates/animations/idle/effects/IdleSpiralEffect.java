package id.seria.gacha.managers.crates.animations.idle.effects;

import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.managers.crates.animations.IdleCrateAnimation;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectAnimation;
import org.bukkit.Location;

public class IdleSpiralEffect extends IdleEffectAnimation {
   private double delta;
   private double theta;
   private boolean flipflop;

   public IdleSpiralEffect(IdleCrateAnimation animation, IdleEffect effect) {
      super(animation, effect);
   }

   @Override
   public void tick() {
      Location center = this.center.clone().subtract(0.0, 0.5, 0.0);

      for (int i = 1; i <= 2; i++) {
         for (double t = 0.0; t <= Math.PI * 2; t += this.steps) {
            double p = this.delta + t;
            double x = MathUtil.sin(p) * 1.5;
            double z = MathUtil.cos(p) * 1.5;
            this.display(center.clone().add(x, this.theta, z));
         }

         this.delta += 0.10471975511965977;
         if (this.delta >= Math.PI * 2) {
            this.delta -= Math.PI * 2;
         }

         if (this.flipflop) {
            this.theta -= 0.1;
            if (this.theta < 0.0) {
               this.theta = 0.0;
               this.flipflop = !this.flipflop;
            }
         } else {
            this.theta += 0.1;
            if (this.theta > 4.0) {
               this.theta = 4.0;
               this.flipflop = !this.flipflop;
            }
         }
      }
   }
}
