package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.util;

import java.lang.reflect.Constructor;
import org.jetbrains.annotations.NotNull;

public class ConstructorInvoker<T> {
   @NotNull
   private final Constructor<T> constructor;

   public ConstructorInvoker(@NotNull Constructor<T> constructor) {
      this.constructor = constructor;
   }

   @NotNull
   public Constructor<T> getConstructor() {
      return this.constructor;
   }

   @NotNull
   public T newInstance(Object... initargs) throws IllegalArgumentException {
      try {
         return this.constructor.newInstance(initargs);
      } catch (ReflectiveOperationException e) {
         throw new WrappedReflectiveOperationException(e);
      }
   }
}
