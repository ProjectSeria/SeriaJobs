package id.seria.gacha.managers.crates.animations.opening.data;

import id.seria.gacha.api.crate.animation.AnimationProperties;

@Deprecated
public abstract class OpeningPhaseData {
   public OpeningPhaseData() {
   }

   public abstract OpeningPhaseData clone();

   public abstract AnimationProperties asProperties();
}
