package id.seria.gacha.lib.xseries.reflection.asm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class ASMPrivateLookup {
   private final Lookup lookup = MethodHandles.lookup();
   private final Class<?> targetClass;

   public ASMPrivateLookup(Class<?> targetClass) {
      this.targetClass = targetClass;
   }

   public MethodHandle findMethod(String name, Class<?> rType, Class<?>[] pTypes) throws IllegalAccessException {
      Method found = new ASMPrivateLookup.ReflectionIterator<Method>(clazz -> clazz.getDeclaredMethod(name, pTypes)).find();
      if (found == null) {
         throw new IllegalArgumentException("Couldn't find method named '" + name + "' with type: " + rType + " (" + Arrays.toString(pTypes) + ')');
      }

      found.setAccessible(true);
      return this.lookup.unreflect(found);
   }

   public MethodHandle findConstructor(Class<?>[] pTypes) throws IllegalAccessException {
      try {
         Constructor<?> found = this.targetClass.getDeclaredConstructor(pTypes);
         found.setAccessible(true);
         return this.lookup.unreflectConstructor(found);
      } catch (NoSuchMethodException e) {
         throw new IllegalArgumentException("Couldn't find constructor with type:  (" + Arrays.toString(pTypes) + ')', e);
      }
   }

   public MethodHandle findField(String name, Class<?> rType, boolean getter) throws IllegalAccessException {
      Field found = new ASMPrivateLookup.ReflectionIterator<Field>(clazz -> clazz.getDeclaredField(name)).find();
      if (found == null) {
         throw new IllegalArgumentException("Couldn't find field named '" + name + "' with type: " + rType);
      }

      found.setAccessible(true);
      return getter ? this.lookup.unreflectGetter(found) : this.lookup.unreflectSetter(found);
   }

   private final class ReflectionIterator<T> {
      private final ASMPrivateLookup.UnsafeFunction<Class<?>, T> finder;

      private ReflectionIterator(ASMPrivateLookup.UnsafeFunction<Class<?>, T> finder) {
         this.finder = finder;
      }

      private T find() {
         try {
            return this.iterate(ASMPrivateLookup.this.targetClass);
         } catch (Exception e) {
            throw new IllegalStateException(e);
         }
      }

      private T iterate(Class<?> clazz) throws Exception {
         if (clazz == Object.class) {
            return null;
         }

         try {
            return this.finder.apply(clazz);
         } catch (NoSuchMethodException | NoSuchFieldException ignored) {
            return this.iterate(clazz.getSuperclass());
         }
      }
   }

   @FunctionalInterface
   private interface UnsafeFunction<I, O> {
      O apply(I var1) throws Exception;
   }
}
