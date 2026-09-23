package id.seria.gacha.managers.crates.animations.opening.data;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import java.awt.Color;
import lombok.Generated;

@Deprecated
@Serializable
public class ColoredOpeningPhaseData extends OpeningPhaseData {
   @ConfigField
   protected Color color = Color.WHITE;

   public ColoredOpeningPhaseData(Color color) {
      this.color = color;
   }

   public ColoredOpeningPhaseData clone() {
      return new ColoredOpeningPhaseData(this.color);
   }

   @Override
   public AnimationProperties asProperties() {
      return AnimationProperties.of("legacyType", "color", "color", this.color);
   }

   @Generated
   public Color getColor() {
      return this.color;
   }

   @Generated
   public void setColor(Color color) {
      this.color = color;
   }

   @Generated
   public ColoredOpeningPhaseData() {
   }
}
