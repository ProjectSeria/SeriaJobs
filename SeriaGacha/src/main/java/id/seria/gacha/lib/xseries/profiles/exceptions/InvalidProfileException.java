package id.seria.gacha.lib.xseries.profiles.exceptions;

import org.jetbrains.annotations.NotNull;

public class InvalidProfileException extends ProfileException {
   private final String value;

   public InvalidProfileException(String value, String message) {
      super(message);
      this.value = value;
   }

   public InvalidProfileException(String value, String message, Throwable cause) {
      super(message, cause);
      this.value = value;
   }

   @NotNull
   public String getValue() {
      return this.value;
   }
}
