package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.util;

import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodInvoker {
   @NotNull
   private final Method method;

   public MethodInvoker(@NotNull Method method) {
      this.method = method;
   }

   @NotNull
   public Method getMethod() {
      return this.method;
   }

   @Nullable
   public Object invoke(@Nullable Object obj, Object... args) throws IllegalArgumentException {
      try {
         return this.method.invoke(obj, args);
      } catch (ReflectiveOperationException e) {
         throw new WrappedReflectiveOperationException(e);
      }
   }
}
