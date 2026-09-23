package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.util;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.reflectionremapper.ReflectionRemapper;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReflectionUtil {
   @NotNull
   public static final Predicate<Member> IS_PUBLIC = member -> Modifier.isPublic(member.getModifiers());
   @NotNull
   public static final Predicate<Member> IS_NOT_PUBLIC = IS_PUBLIC.negate();
   @NotNull
   public static final Predicate<Member> IS_PRIVATE = member -> Modifier.isPrivate(member.getModifiers());
   @NotNull
   public static final Predicate<Member> IS_NOT_PRIVATE = IS_PRIVATE.negate();
   @NotNull
   public static final Predicate<Member> IS_STATIC = member -> Modifier.isStatic(member.getModifiers());
   @NotNull
   public static final Predicate<Member> IS_NOT_STATIC = IS_STATIC.negate();
   @NotNull
   public static final Predicate<Member> IS_FINAL = member -> Modifier.isFinal(member.getModifiers());
   @NotNull
   public static final Predicate<Member> IS_NOT_FINAL = IS_FINAL.negate();

   private ReflectionUtil() {
      throw new UnsupportedOperationException("This class cannot be instantiated");
   }

   @NotNull
   private static Field makeFieldAccessible(@NotNull Field field) {
      if (!field.isAccessible()) {
         field.setAccessible(true);
      }

      return field;
   }

   @NotNull
   private static Method makeMethodAccessible(@NotNull Method method) {
      if (!method.isAccessible()) {
         method.setAccessible(true);
      }

      return method;
   }

   @NotNull
   public static <T> Class<T> getClass(@NotNull String className) {
      if (ServerVersions.isPaper() && MinecraftVersions.TRAILS_AND_TAILS.get(5).isAtLeast()) {
         ReflectionRemapper remapper = ReflectionRemapper.forReobfMappingsInPaperJar();
         className = remapper.remapClassOrArrayName(className);
      }

      try {
         return (Class<T>)Class.forName(className);
      } catch (ClassNotFoundException e) {
         throw new WrappedReflectiveOperationException(e);
      }
   }

   @NotNull
   public static <T> Class<T> getMinecraftClass(@NotNull String packageName, @NotNull String className) {
      return MinecraftVersions.CAVES_AND_CLIFFS_1.isAtLeast()
         ? getClass("net.minecraft." + packageName + "." + className)
         : getClass("net.minecraft.server." + MinecraftVersions.getCurrent() + "." + className);
   }

   @NotNull
   public static <T> Class<T> getCraftBukkitClass(@NotNull String classPath) {
      return ServerVersions.isPaper() && MinecraftVersions.TRAILS_AND_TAILS.get(5).isAtLeast()
         ? getClass("org.bukkit.craftbukkit." + classPath)
         : getClass("org.bukkit.craftbukkit." + MinecraftVersions.getCurrent() + "." + classPath);
   }

   @NotNull
   public static FieldAccessor getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
      try {
         if (ServerVersions.isPaper() && MinecraftVersions.TRAILS_AND_TAILS.get(5).isAtLeast()) {
            ReflectionRemapper remapper = ReflectionRemapper.forReobfMappingsInPaperJar();
            fieldName = remapper.remapFieldName(clazz, fieldName);
         }

         return new FieldAccessor(makeFieldAccessible(clazz.getDeclaredField(fieldName)));
      } catch (ReflectiveOperationException e) {
         throw new WrappedReflectiveOperationException(e);
      }
   }

   @NotNull
   public static FieldAccessor getField(@NotNull Class<?> clazz, @NotNull Class<?> fieldType) {
      return getField(clazz, fieldType, 0, null);
   }

   @NotNull
   public static FieldAccessor getField(@NotNull Class<?> clazz, @NotNull Class<?> fieldType, int index) {
      return getField(clazz, fieldType, index, null);
   }

   @NotNull
   public static FieldAccessor getField(@NotNull Class<?> clazz, @NotNull Class<?> fieldType, int index, @Nullable Predicate<? super Field> predicate) {
      for (Field field : clazz.getDeclaredFields()) {
         if (fieldType.isAssignableFrom(field.getType()) && (predicate == null || predicate.test(field)) && index-- <= 0) {
            return new FieldAccessor(makeFieldAccessible(field));
         }
      }

      try {
         if (clazz.getSuperclass() != null) {
            return getField(clazz.getSuperclass(), fieldType, index, predicate);
         }
      } catch (IllegalArgumentException ex) {
         if (!ex.getMessage().startsWith("No field of type ")) {
            throw ex;
         }
      }

      throw new IllegalArgumentException("No field of type " + fieldType.getName() + " found in class " + clazz.getName());
   }

   @NotNull
   public static MethodInvoker getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... parameterTypes) {
      try {
         if (ServerVersions.isPaper() && MinecraftVersions.TRAILS_AND_TAILS.get(5).isAtLeast()) {
            ReflectionRemapper remapper = ReflectionRemapper.forReobfMappingsInPaperJar();
            methodName = remapper.remapMethodName(clazz, methodName);
         }

         return new MethodInvoker(makeMethodAccessible(clazz.getDeclaredMethod(methodName, parameterTypes)));
      } catch (ReflectiveOperationException e) {
         throw new WrappedReflectiveOperationException(e);
      }
   }

   @NotNull
   public static MethodInvoker getMethod(@NotNull Class<?> clazz, @NotNull Class<?> returnType, Class<?>... parameterTypes) {
      return getMethod(clazz, returnType, 0, null, parameterTypes);
   }

   @NotNull
   public static MethodInvoker getMethod(@NotNull Class<?> clazz, @NotNull Class<?> returnType, int index, Class<?>... parameterTypes) {
      return getMethod(clazz, returnType, index, null, parameterTypes);
   }

   @NotNull
   public static MethodInvoker getMethod(
      @NotNull Class<?> clazz, @NotNull Class<?> returnType, int index, @Nullable Predicate<? super Method> predicate, Class<?>... parameterTypes
   ) {
      for (Method method : clazz.getDeclaredMethods()) {
         if (returnType.isAssignableFrom(method.getReturnType())
            && (predicate == null || predicate.test(method))
            && parameterTypes.length == method.getParameterCount()) {
            boolean match = true;

            for (int i = 0; i < parameterTypes.length; i++) {
               if (!parameterTypes[i].isAssignableFrom(method.getParameterTypes()[i])) {
                  match = false;
                  break;
               }
            }

            if (match && index-- <= 0) {
               return new MethodInvoker(makeMethodAccessible(method));
            }
         }
      }

      try {
         if (clazz.getSuperclass() != null) {
            return getMethod(clazz.getSuperclass(), returnType, index, predicate, parameterTypes);
         }
      } catch (IllegalArgumentException ex) {
         if (!ex.getMessage().startsWith("No method with return type ")) {
            throw ex;
         }
      }

      throw new IllegalArgumentException("No method with return type " + returnType.getName() + " found in class " + clazz.getName());
   }

   @NotNull
   public static <T> ConstructorInvoker<T> getConstructor(@NotNull Class<T> clazz, Class<?>... parameterTypes) {
      try {
         Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
         return new ConstructorInvoker<>(constructor);
      } catch (ReflectiveOperationException e) {
         throw new WrappedReflectiveOperationException(e);
      }
   }
}
