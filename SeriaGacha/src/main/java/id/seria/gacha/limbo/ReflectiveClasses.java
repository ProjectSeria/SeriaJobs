package id.seria.gacha.limbo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class ReflectiveClasses {
   private ReflectiveClasses() {
   }

   public static <T> T newInstanceOrNull(String className) {
      try {
         Class<?> clazz = Class.forName(className);
         Constructor<?> ctor = clazz.getDeclaredConstructor();
         ctor.setAccessible(true);
         return (T)ctor.newInstance();
      } catch (Throwable ignored) {
         return null;
      }
   }

   public static <T> T newInstanceOrNull(String className, Class<?> paramType, Object arg) {
      try {
         Class<?> clazz = Class.forName(className);
         Constructor<?> ctor = clazz.getDeclaredConstructor(paramType);
         ctor.setAccessible(true);
         return (T)ctor.newInstance(arg);
      } catch (Throwable ignored) {
         return null;
      }
   }

   public static <T> T newInstanceMatchingOrNull(String className, Object... args) {
      try {
         Class<?> clazz = Class.forName(className);

         label36:
         for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            Class<?>[] paramTypes = ctor.getParameterTypes();
            if (paramTypes.length == args.length) {
               for (int i = 0; i < paramTypes.length; i++) {
                  if (args[i] != null && !paramTypes[i].isAssignableFrom(args[i].getClass())) {
                     continue label36;
                  }
               }

               ctor.setAccessible(true);
               return (T)ctor.newInstance(args);
            }
         }

         return null;
      } catch (Throwable ignored) {
         return null;
      }
   }

   public static boolean isPresent(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (Throwable ignored) {
         return false;
      }
   }

   public static void invokeStaticNoArgs(String className, String methodName) {
      try {
         Class<?> clazz = Class.forName(className);
         Method method = clazz.getDeclaredMethod(methodName);
         method.setAccessible(true);
         method.invoke(null);
      } catch (Throwable var4) {
      }
   }
}
