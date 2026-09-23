package id.seria.gacha.managers.crates.animations.idle.effects;

import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.managers.crates.animations.IdleCrateAnimation;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectAnimation;
import org.bukkit.Location;

public class IdleFlowerEffect extends IdleEffectAnimation {
   private double delta;

   public IdleFlowerEffect(IdleCrateAnimation animation, IdleEffect effect) {
      super(animation, effect);
   }

   @Override
   public void tick() {
      double a = 7.0;
      Location center = this.center.clone().subtract(0.0, 0.5, 0.0);

      for (int i = 1; i <= 2; i++) {
         for (double t = 0.0; t <= Math.PI * 2; t += this.steps) {
            double p = this.delta + t;
            double x = 6.0 * MathUtil.sin(p) + MathUtil.sin(6.0 * p);
            double z = 6.0 * MathUtil.cos(p) + MathUtil.cos(6.0 * p);
            this.display(center.clone().add(x / 3.0, 0.1, z / 3.0));
         }

         this.delta += 0.07853981633974483;
         if (this.delta >= Math.PI * 2) {
            this.delta -= Math.PI * 2;
         }
      }
   }
}
