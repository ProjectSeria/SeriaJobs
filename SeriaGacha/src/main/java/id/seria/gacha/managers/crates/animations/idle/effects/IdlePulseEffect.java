package id.seria.gacha.managers.crates.animations.idle.effects;

import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.managers.crates.animations.IdleCrateAnimation;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectAnimation;
import org.bukkit.Location;

public class IdlePulseEffect extends IdleEffectAnimation {
   private double delta;

   public IdlePulseEffect(IdleCrateAnimation animation, IdleEffect effect) {
      super(animation, effect);
   }

   @Override
   public void tick() {
      if (this.delta > 2.5) {
         this.delta = 1.0;
      }

      Location center = this.center.clone().subtract(0.0, 0.5, 0.0);

      for (int i = 1; i <= this.amount; i++) {
         double deltaTemp = this.normalizeDelta(this.delta + (i - 1) * (1.5 / i));
         if (deltaTemp == 0.0) {
            deltaTemp = 0.07853981633974483;
         }

         for (double t = 0.0; t <= Math.PI * 2; t += Math.PI / (deltaTemp * 15.0)) {
            double p = t;
            double x = MathUtil.sin(p) * deltaTemp;
            double z = MathUtil.cos(p) * deltaTemp;
            this.display(center.clone().add(x, 0.1, z));
         }
      }

      this.delta += 0.07853981633974483;
      this.delta = this.normalizeDelta(this.delta);
   }

   private double normalizeDelta(double delta) {
      if (delta > 1.5) {
         while (delta > 1.5) {
            delta -= 1.5;
         }
      } else if (delta < 0.0) {
         while (delta < 0.0) {
            delta += 1.5;
         }
      }

      return delta;
   }
}
