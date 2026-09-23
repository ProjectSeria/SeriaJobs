package id.seria.gacha.managers.crates.animations.idle.effects;

import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.managers.crates.animations.IdleCrateAnimation;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectAnimation;
import org.bukkit.Location;

public class IdleSquareEffect extends IdleEffectAnimation {
   private double delta;

   public IdleSquareEffect(IdleCrateAnimation animation, IdleEffect effect) {
      super(animation, effect);
   }

   @Override
   public void tick() {
      Location center = this.center.clone().subtract(0.0, 0.5, 0.0);

      for (int i = 1; i <= 2; i++) {
         for (double t = 0.0; t <= Math.PI * 2; t += this.steps) {
            double p = this.delta + t;
            double sin = MathUtil.sin(p);
            double cos = MathUtil.cos(p);
            double mult1 = MathUtil.sin(p - sin) + MathUtil.sin(p + sin);
            double mult2 = MathUtil.cos(p - cos) + MathUtil.cos(p + cos);
            this.display(center.clone().add(mult1 * 1.5, 0.1, mult2 * 1.5));
         }

         this.delta += 0.07853981633974483;
         if (this.delta >= Math.PI * 2) {
            this.delta -= Math.PI * 2;
         }
      }
   }
}
