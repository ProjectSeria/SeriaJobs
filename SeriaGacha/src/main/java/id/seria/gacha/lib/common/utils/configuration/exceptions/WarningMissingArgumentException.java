package id.seria.gacha.lib.common.utils.configuration.exceptions;

import lombok.Generated;

public class WarningMissingArgumentException extends WarningException {
   private final Object buildedObject;

   public WarningMissingArgumentException(String message, Object buildedObject) {
      super(message);
      this.buildedObject = buildedObject;
   }

   public WarningMissingArgumentException(String message) {
      this(message, null);
   }

   @Generated
   public Object getBuildedObject() {
      return this.buildedObject;
   }
}
