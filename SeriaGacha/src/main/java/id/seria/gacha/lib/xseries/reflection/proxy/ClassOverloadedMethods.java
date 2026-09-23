package id.seria.gacha.lib.xseries.reflection.proxy;

import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class ClassOverloadedMethods<T> {
   private final Map<String, OverloadedMethod<T>> mapped;

   public ClassOverloadedMethods(Map<String, OverloadedMethod<T>> mapped) {
      this.mapped = mapped;
   }

   public Map<String, OverloadedMethod<T>> mappings() {
      return this.mapped;
   }

   public T get(String name, Supplier<String> descriptor) {
      return this.get(name, descriptor, false);
   }

   public T get(String name, Supplier<String> descriptor, boolean ignoreIfNull) {
      OverloadedMethod<T> overloads = this.mapped.get(name);
      if (overloads == null) {
         if (ignoreIfNull) {
            return null;
         } else {
            throw new IllegalArgumentException("Failed to find any method named '" + name + "' with descriptor '" + descriptor.get() + "' " + this);
         }
      } else {
         T overload = overloads.get(descriptor);
         if (overload == null) {
            if (ignoreIfNull) {
               return null;
            } else {
               throw new IllegalArgumentException("Failed to find overloaded method named '" + name + " with descriptor: '" + descriptor.get() + "' " + this);
            }
         } else {
            return overload;
         }
      }
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + '(' + this.mapped + ')';
   }
}
