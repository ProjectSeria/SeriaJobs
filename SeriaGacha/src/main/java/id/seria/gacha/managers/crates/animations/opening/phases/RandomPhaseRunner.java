package id.seria.gacha.managers.crates.animations.opening.phases;

import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.animation.AnimationPhase;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.registries.AnimationsRegistry;
import id.seria.gacha.registries.animation.AddonLockedAnimationPhaseImpl;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomPhaseRunner extends PhaseRunnerImpl {
   private PhaseRunnerImpl delegate;

   public RandomPhaseRunner(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void load() {
      Collection<AnimationPhase> allPhases = AnimationsRegistry.getInstance().getPhases(this.phase.getType());
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      List<AnimationPhase> candidates = new ArrayList<>();

      for (AnimationPhase phase : allPhases) {
         if (!phase.getId().equals("RANDOM")
            && !phase.getId().equals("NONE")
            && !(phase instanceof AddonLockedAnimationPhaseImpl)
            && (!lite || !phase.isPremium())
            && phase.isVersionCompatible()) {
            candidates.add(phase);
         }
      }

      if (candidates.isEmpty()) {
         this.nextPhase();
      } else {
         AnimationPhase chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
         this.delegate = (PhaseRunnerImpl)chosen.createRunner(this.handler, this.properties);
         this.delegate.load();
      }
   }

   @Override
   public void tick() {
      if (this.delegate == null) {
         this.nextPhase();
      } else {
         this.delegate.tick();
      }
   }

   @Override
   public void unload() {
      if (this.delegate != null) {
         this.delegate.unload();
      }
   }
}
