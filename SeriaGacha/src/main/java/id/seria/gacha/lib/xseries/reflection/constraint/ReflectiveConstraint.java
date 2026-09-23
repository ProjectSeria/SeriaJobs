package id.seria.gacha.lib.xseries.reflection.constraint;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Experimental
public interface ReflectiveConstraint {
   @Contract(pure = true)
   String category();

   @Contract(pure = true)
   String name();

   @NotNull
   @Contract(pure = true)
   ReflectiveConstraint.Result appliesTo(@NotNull ReflectiveHandle<?> var1, @NotNull Object var2);

   enum Result {
      INCOMPATIBLE,
      NOT_MATCHED,
      MATCHED;

      Result() {
      }

      @Internal
      public static ReflectiveConstraint.Result of(boolean test) {
         return test ? MATCHED : NOT_MATCHED;
      }
   }
}
