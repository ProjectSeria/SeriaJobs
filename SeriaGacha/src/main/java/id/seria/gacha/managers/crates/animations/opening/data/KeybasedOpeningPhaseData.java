package id.seria.gacha.managers.crates.animations.opening.data;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import lombok.Generated;

@Deprecated
@Serializable
public class KeybasedOpeningPhaseData extends OpeningPhaseData {
   @ConfigField
   protected boolean useKeyAsModel = false;
   @ConfigField
   protected KeybasedOpeningPhaseData.KeyAlignment alignment = KeybasedOpeningPhaseData.KeyAlignment.VERTICAL;
   @ConfigField
   protected double offsetX = 0.0;
   @ConfigField
   protected double offsetY = 0.0;
   @ConfigField
   protected double offsetZ = 0.0;

   public KeybasedOpeningPhaseData(boolean useKeyAsModel, KeybasedOpeningPhaseData.KeyAlignment alignment, double offsetX, double offsetY, double offsetZ) {
      this.useKeyAsModel = useKeyAsModel;
      this.alignment = alignment;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
   }

   public KeybasedOpeningPhaseData clone() {
      return new KeybasedOpeningPhaseData(this.useKeyAsModel, this.alignment, this.offsetX, this.offsetY, this.offsetZ);
   }

   @Override
   public AnimationProperties asProperties() {
      return AnimationProperties.of(
         "legacyType",
         "key",
         "useKeyAsModel",
         this.useKeyAsModel,
         "alignment",
         this.alignment.name(),
         "offsetX",
         this.offsetX,
         "offsetY",
         this.offsetY,
         "offsetZ",
         this.offsetZ
      );
   }

   @Generated
   public boolean isUseKeyAsModel() {
      return this.useKeyAsModel;
   }

   @Generated
   public KeybasedOpeningPhaseData.KeyAlignment getAlignment() {
      return this.alignment;
   }

   @Generated
   public double getOffsetX() {
      return this.offsetX;
   }

   @Generated
   public double getOffsetY() {
      return this.offsetY;
   }

   @Generated
   public double getOffsetZ() {
      return this.offsetZ;
   }

   @Generated
   public void setUseKeyAsModel(boolean useKeyAsModel) {
      this.useKeyAsModel = useKeyAsModel;
   }

   @Generated
   public void setAlignment(KeybasedOpeningPhaseData.KeyAlignment alignment) {
      this.alignment = alignment;
   }

   @Generated
   public void setOffsetX(double offsetX) {
      this.offsetX = offsetX;
   }

   @Generated
   public void setOffsetY(double offsetY) {
      this.offsetY = offsetY;
   }

   @Generated
   public void setOffsetZ(double offsetZ) {
      this.offsetZ = offsetZ;
   }

   @Generated
   public KeybasedOpeningPhaseData() {
   }

   public enum KeyAlignment {
      HORIZONTAL,
      VERTICAL,
      INVERTED_HORIZONTAL,
      INVERTED_VERTICAL;

      KeyAlignment() {
      }

      public boolean isVertical() {
         return this.equals(INVERTED_VERTICAL) || this.equals(VERTICAL);
      }

      public boolean isHorizontal() {
         return this.equals(INVERTED_HORIZONTAL) || this.equals(HORIZONTAL);
      }

      public boolean isInverted() {
         return this.equals(INVERTED_HORIZONTAL) || this.equals(INVERTED_VERTICAL);
      }
   }
}
