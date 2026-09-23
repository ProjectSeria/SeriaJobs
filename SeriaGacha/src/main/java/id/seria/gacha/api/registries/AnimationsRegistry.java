package id.seria.gacha.api.registries;

import id.seria.gacha.api.crate.animation.AnimationPhase;
import id.seria.gacha.api.crate.animation.PhaseType;
import java.util.Collection;
import java.util.Optional;

public interface AnimationsRegistry {
   void registerPhase(AnimationPhase var1);

   void unregisterPhase(AnimationPhase var1);

   Collection<AnimationPhase> getPhases(PhaseType var1);

   Optional<AnimationPhase> getPhaseById(PhaseType var1, String var2);
}
