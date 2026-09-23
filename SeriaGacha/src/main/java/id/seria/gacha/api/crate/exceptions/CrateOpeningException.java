package id.seria.gacha.api.crate.exceptions;

import lombok.Generated;

public class CrateOpeningException extends Exception {
   private boolean knockbackActive = true;

   public CrateOpeningException(String message) {
      super(message);
   }

   public CrateOpeningException(String message, boolean knockbackActive) {
      super(message);
      this.knockbackActive = knockbackActive;
   }

   @Generated
   public boolean isKnockbackActive() {
      return this.knockbackActive;
   }

   @Generated
   public void setKnockbackActive(boolean knockbackActive) {
      this.knockbackActive = knockbackActive;
   }
}
