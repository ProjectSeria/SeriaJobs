package id.seria.gacha.managers.crates.animations.opening;

import id.seria.gacha.api.crate.animation.AnimationPhase;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.managers.crates.animations.opening.data.OpeningPhaseData;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import lombok.Generated;

public class OpeningPhase implements Cloneable {
   private final AnimationPhase phase;
   private AnimationProperties properties;

   @Deprecated
   public OpeningPhase(AnimationPhase type, OpeningPhaseData data) {
      this.phase = type;
      this.properties = data.asProperties();
   }

   public OpeningPhase(AnimationPhase type, AnimationProperties properties) {
      this.phase = type;
      this.properties = properties;
   }

   public OpeningPhase(AnimationPhaseImpl phase) {
      this.phase = phase;
      this.properties = AnimationProperties.empty();
   }

   public OpeningPhase clone() {
      return new OpeningPhase(this.phase, this.properties.clone());
   }

   @Generated
   public AnimationPhase getPhase() {
      return this.phase;
   }

   @Generated
   public AnimationProperties getProperties() {
      return this.properties;
   }

   @Generated
   public void setProperties(AnimationProperties properties) {
      this.properties = properties;
   }
}
