package id.seria.gacha.managers.crates.animations.opening.phases.first;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;

public class FirstPhaseDefaultAnimation extends PhaseRunnerImpl {
   public FirstPhaseDefaultAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
   }

   @Override
   public void tick() {
      this.nextPhase();
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
