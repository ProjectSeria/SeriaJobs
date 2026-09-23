package id.seria.gacha.sdk.utilities;

import java.lang.reflect.Method;

public class Reflections {
   public Reflections() {
   }

   public static boolean hasMethod(Class<?> clazz, String methodName) {
      for (Method method : clazz.getDeclaredMethods()) {
         if (method.getName().equals(methodName)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasMethod(Object obj, String methodName) {
      return obj == null ? false : hasMethod(obj.getClass(), methodName);
   }

   public static <T> T invokeOrDefault(Object obj, String method, T defaultValue) {
      if (obj == null) {
         return defaultValue;
      }

      try {
         Method m = obj.getClass().getMethod(method);
         m.setAccessible(true);
         return (T)m.invoke(obj);
      } catch (NoSuchMethodException e) {
         return defaultValue;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public static <T> T invokeOrNull(Object obj, String method, Class<T> returnType) {
      if (obj == null) {
         return null;
      }

      try {
         Method m = obj.getClass().getMethod(method);
         m.setAccessible(true);
         Object result = m.invoke(obj);
         return returnType.isInstance(result) ? returnType.cast(result) : null;
      } catch (NoSuchMethodException e) {
         return null;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public static boolean invokeIfExists(Object obj, String methodName, Class<?>[] paramTypes, Object... args) {
      if (obj != null && methodName != null) {
         try {
            Method m = obj.getClass().getMethod(methodName, paramTypes);
            m.setAccessible(true);
            m.invoke(obj, args);
            return true;
         } catch (NoSuchMethodException e) {
            return false;
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      } else {
         return false;
      }
   }
}
