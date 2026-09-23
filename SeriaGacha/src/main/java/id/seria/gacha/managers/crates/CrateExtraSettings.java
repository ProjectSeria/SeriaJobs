package id.seria.gacha.managers.crates;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import lombok.Generated;
import org.bukkit.util.Vector;

public class CrateExtraSettings implements Cloneable {
   @ConfigField(path = "animation.display-item-position")
   private Vector displayItemPosition = new Vector(0, 2, 0);
   @ConfigField(path = "animation.display-item-name-position")
   private Vector displayItemNamePosition = new Vector(0.0, 0.3, 0.0);
   @ConfigField(path = "animation.skip-on-esc")
   private boolean skipOnEsc = false;

   public CrateExtraSettings clone() {
      CrateExtraSettings cloned = new CrateExtraSettings();
      cloned.displayItemPosition = this.displayItemPosition.clone();
      cloned.displayItemNamePosition = this.displayItemNamePosition.clone();
      cloned.skipOnEsc = this.skipOnEsc;
      return cloned;
   }

   @Generated
   public Vector getDisplayItemPosition() {
      return this.displayItemPosition;
   }

   @Generated
   public Vector getDisplayItemNamePosition() {
      return this.displayItemNamePosition;
   }

   @Generated
   public boolean isSkipOnEsc() {
      return this.skipOnEsc;
   }

   @Generated
   public CrateExtraSettings() {
   }

   @Generated
   public void setSkipOnEsc(boolean skipOnEsc) {
      this.skipOnEsc = skipOnEsc;
   }
}
