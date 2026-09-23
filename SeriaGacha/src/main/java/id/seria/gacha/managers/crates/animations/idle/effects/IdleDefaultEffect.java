package id.seria.gacha.managers.crates.animations.idle.effects;

import id.seria.gacha.managers.crates.animations.IdleCrateAnimation;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectAnimation;

public class IdleDefaultEffect extends IdleEffectAnimation {
   private int delta = 0;

   public IdleDefaultEffect(IdleCrateAnimation animation, IdleEffect effect) {
      super(animation, effect);
   }

   @Override
   public void tick() {
      if (this.delta++ % 2 == 0) {
         for (int i = 0; i < this.amount; i++) {
            this.display(this.center);
         }
      }
   }
}
