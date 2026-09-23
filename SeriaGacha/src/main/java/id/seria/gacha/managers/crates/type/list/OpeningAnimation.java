package id.seria.gacha.managers.crates.type.list;

import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.EmptyOpeningPhaseData;
import id.seria.gacha.registries.AnimationsRegistry;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Generated;

public class OpeningAnimation implements Cloneable {
   private OpeningPhase preOpenPhase;
   private OpeningPhase openPhase;
   private OpeningPhase displayPhase;

   public static OpeningAnimation defaultAnimation() {
      return new OpeningAnimation(
         new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.PRE_OPENING, "DEFAULT").get(), new EmptyOpeningPhaseData()),
         new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.OPENING, "NONE").get(), new EmptyOpeningPhaseData()),
         new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.DISPLAYING, "DEFAULT").get(), new ColoredOpeningPhaseData(Color.WHITE))
      );
   }

   protected OpeningAnimation(OpeningPhase preOpenPhase, OpeningPhase openPhase, OpeningPhase displayPhase) {
      this.preOpenPhase = preOpenPhase;
      this.openPhase = openPhase;
      this.displayPhase = displayPhase;
   }

   public Map<PhaseType, OpeningPhase> map() {
      Map<PhaseType, OpeningPhase> map = new LinkedHashMap<>();
      if (this.preOpenPhase != null) {
         map.put(PhaseType.PRE_OPENING, this.preOpenPhase);
      }

      if (this.openPhase != null) {
         map.put(PhaseType.OPENING, this.openPhase);
      }

      if (this.displayPhase != null) {
         map.put(PhaseType.DISPLAYING, this.displayPhase);
      }

      return map;
   }

   public OpeningAnimation clone() {
      return new OpeningAnimation(
         this.preOpenPhase == null ? null : this.preOpenPhase.clone(),
         this.openPhase == null ? null : this.openPhase.clone(),
         this.displayPhase == null ? null : this.displayPhase.clone()
      );
   }

   @Generated
   public void setPreOpenPhase(OpeningPhase preOpenPhase) {
      this.preOpenPhase = preOpenPhase;
   }

   @Generated
   public void setOpenPhase(OpeningPhase openPhase) {
      this.openPhase = openPhase;
   }

   @Generated
   public void setDisplayPhase(OpeningPhase displayPhase) {
      this.displayPhase = displayPhase;
   }

   @Generated
   public OpeningPhase getPreOpenPhase() {
      return this.preOpenPhase;
   }

   @Generated
   public OpeningPhase getOpenPhase() {
      return this.openPhase;
   }

   @Generated
   public OpeningPhase getDisplayPhase() {
      return this.displayPhase;
   }
}
