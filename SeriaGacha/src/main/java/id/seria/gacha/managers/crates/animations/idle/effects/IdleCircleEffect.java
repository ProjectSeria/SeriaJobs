package id.seria.gacha.managers.crates.animations.idle.effects;

import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.managers.crates.animations.IdleCrateAnimation;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectAnimation;
import org.bukkit.Location;

public class IdleCircleEffect extends IdleEffectAnimation {
   private double delta;

   public IdleCircleEffect(IdleCrateAnimation animation, IdleEffect effect) {
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
            this.display(center.clone().add(x, 0.1, z));
         }

         this.delta += 0.07853981633974483;
         if (this.delta >= Math.PI * 2) {
            this.delta -= Math.PI * 2;
         }
      }
   }
}
