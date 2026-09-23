package id.seria.gacha.managers.crates.animations.opening.data;

import id.seria.gacha.api.crate.animation.AnimationProperties;

@Deprecated
public class EmptyOpeningPhaseData extends OpeningPhaseData {
   public EmptyOpeningPhaseData() {
   }

   public EmptyOpeningPhaseData clone() {
      return new EmptyOpeningPhaseData();
   }

   @Override
   public AnimationProperties asProperties() {
      return AnimationProperties.of("legacyType", "empty");
   }
}
