package id.seria.gacha.lib.xseries.profiles.exceptions;

import org.jetbrains.annotations.NotNull;

public final class InvalidProfileContainerException extends ProfileException {
   private final Object container;

   public InvalidProfileContainerException(Object container, String message) {
      super(message);
      this.container = container;
   }

   @NotNull
   public Object getContainer() {
      return this.container;
   }
}
