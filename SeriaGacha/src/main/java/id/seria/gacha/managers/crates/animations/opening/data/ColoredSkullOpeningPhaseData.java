package id.seria.gacha.managers.crates.animations.opening.data;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import java.awt.Color;
import lombok.Generated;

@Deprecated
@Serializable
public class ColoredSkullOpeningPhaseData extends OpeningPhaseData {
   @ConfigField
   protected Color color = Color.WHITE;
   @ConfigField
   private String textureSkull = "f98bc63f05f6378bf29ef10e3d82acb3ceb73a720bf80f30bc576d0ad8c40cfb";

   public ColoredSkullOpeningPhaseData(Color color, String textureSkull) {
      this.color = color;
      this.textureSkull = textureSkull;
   }

   public ColoredSkullOpeningPhaseData clone() {
      return new ColoredSkullOpeningPhaseData(this.color, this.textureSkull);
   }

   @Override
   public AnimationProperties asProperties() {
      return AnimationProperties.of("legacyType", "skull", "color", this.color, "textureSkull", this.textureSkull);
   }

   @Generated
   public Color getColor() {
      return this.color;
   }

   @Generated
   public String getTextureSkull() {
      return this.textureSkull;
   }

   @Generated
   public void setColor(Color color) {
      this.color = color;
   }

   @Generated
   public void setTextureSkull(String textureSkull) {
      this.textureSkull = textureSkull;
   }

   @Generated
   public ColoredSkullOpeningPhaseData() {
   }
}
