package id.seria.gacha.managers.crates.animations.idle.effects;

import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.managers.crates.animations.IdleCrateAnimation;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectAnimation;
import org.bukkit.Location;

public class IdleConicalSpiralEffect extends IdleEffectAnimation {
   private double delta;
   private double theta;

   public IdleConicalSpiralEffect(IdleCrateAnimation animation, IdleEffect effect) {
      super(animation, effect);
   }

   @Override
   public void tick() {
      Location center = this.center.clone().subtract(0.0, 0.5, 0.0);

      for (int i = 1; i <= 2; i++) {
         for (double t = 0.0; t <= Math.PI * 2; t += this.steps) {
            double p = this.delta + t;
            double s = MathUtil.sin(this.theta * 0.385 + 1.6) * 1.5;
            double x = MathUtil.sin(p) * s;
            double z = MathUtil.cos(p) * s;
            this.display(center.clone().add(x, this.theta, z));
         }

         this.delta += 0.10471975511965977;
         if (this.delta >= Math.PI * 2) {
            this.delta -= Math.PI * 2;
         }

         this.theta += 0.05;
         if (this.theta > 4.0) {
            this.theta = 0.0;
         }
      }
   }
}
