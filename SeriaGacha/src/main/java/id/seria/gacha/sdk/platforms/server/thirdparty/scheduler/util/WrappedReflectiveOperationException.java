package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.util;

import org.jetbrains.annotations.NotNull;

public class WrappedReflectiveOperationException extends RuntimeException {
   public WrappedReflectiveOperationException(@NotNull ReflectiveOperationException e) {
      super(e);
   }
}
