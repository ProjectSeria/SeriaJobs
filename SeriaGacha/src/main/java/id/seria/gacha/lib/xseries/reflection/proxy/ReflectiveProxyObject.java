package id.seria.gacha.lib.xseries.reflection.proxy;

import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Ignore;
import java.lang.reflect.Array;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@Experimental
public interface ReflectiveProxyObject {
   @Ignore
   @NotNull
   @NonExtendable
   @Contract(pure = true)
   Object instance();

   @Ignore
   @NotNull
   @NonExtendable
   @Contract(pure = true)
   Class<?> getTargetClass();

   @Ignore
   @NotNull
   @NonExtendable
   @Contract(pure = true)
   default boolean isInstance(@Nullable Object object) {
      return this.getTargetClass().isInstance(object.getClass());
   }

   @Ignore
   @NotNull
   @NonExtendable
   @Contract(pure = true)
   default Object[] newArray(@Range(from = 0L, to = 2147483647L) int length) {
      return (Object[])Array.newInstance(this.getTargetClass(), length);
   }

   @Ignore
   @NotNull
   @NonExtendable
   @Contract(pure = true)
   default Object[] newArray(@Range(from = 0L, to = 2147483647L) int... dimensions) {
      return (Object[])Array.newInstance(this.getTargetClass(), dimensions);
   }

   @Ignore
   @NotNull
   @OverrideOnly
   @Contract(value = "_ -> new", pure = true)
   ReflectiveProxyObject bindTo(@NotNull Object var1);
}
