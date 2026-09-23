package id.seria.gacha.lib.xseries.reflection;

import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public interface ReflectiveHandle<T> {
   @Experimental
   @Contract(value = "-> new", pure = true)
   ReflectiveHandle<T> copy();

   @Contract(pure = true)
   default boolean exists() {
      try {
         this.reflect();
         return true;
      } catch (ReflectiveOperationException ignored) {
         return false;
      }
   }

   @Deprecated
   @Nullable
   @Obsolete
   default ReflectiveOperationException catchError() {
      try {
         this.reflect();
         return null;
      } catch (ReflectiveOperationException ex) {
         return ex;
      }
   }

   @NotNull
   @Contract(pure = true)
   default T unreflect() {
      try {
         return this.reflect();
      } catch (ReflectiveOperationException e) {
         throw XReflection.throwCheckedException(e);
      }
   }

   @Nullable
   @Contract(pure = true)
   default T reflectOrNull() {
      try {
         return this.reflect();
      } catch (ReflectiveOperationException ignored) {
         return null;
      }
   }

   @NotNull
   @Contract(pure = true)
   T reflect() throws ReflectiveOperationException;

   @NotNull
   @Experimental
   @Contract(pure = true)
   ReflectiveHandle<ReflectedObject> jvm();

   @NotNull
   @Experimental
   @Contract(value = "-> new", pure = true)
   default ReflectiveHandle<T> cached() {
      return new CachedReflectiveHandle<>(this.copy());
   }

   @NotNull
   @Experimental
   @Contract(pure = true)
   default ReflectiveHandle<T> unwrap() {
      return this instanceof CachedReflectiveHandle ? ((CachedReflectiveHandle)this).getDelegate() : this;
   }
}
