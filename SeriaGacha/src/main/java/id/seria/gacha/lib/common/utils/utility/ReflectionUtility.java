package id.seria.gacha.lib.common.utils.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtility {
   private static final Map<Class<?>, Map<String, Field>> fieldCache = new ConcurrentHashMap<>();
   private static final Map<Class<?>, Map<String, Method>> methodCache = new ConcurrentHashMap<>();

   public ReflectionUtility() {
   }

   public static Object getStaticField(Class<?> clazz, String fieldName) throws NoSuchFieldException, IllegalAccessException {
      Field field = getCachedField(clazz, fieldName);
      field.setAccessible(true);
      return field.get(null);
   }

   public static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
      Field field = getCachedField(obj.getClass(), fieldName);
      field.setAccessible(true);
      return field.get(obj);
   }

   public static void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
      Field field = getCachedField(obj.getClass(), fieldName);
      field.setAccessible(true);
      field.set(obj, value);
   }

   public static Object createInstance(Class<?> clazz, Class<?>[] constructorParamTypes, Object... constructorArgs) throws Exception {
      Constructor<?> constructor = clazz.getDeclaredConstructor(constructorParamTypes);
      constructor.setAccessible(true);
      return constructor.newInstance(constructorArgs);
   }

   public static Object invokeInstanceMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
      Method method = getCachedMethod(obj.getClass(), methodName, parameterTypes);
      method.setAccessible(true);
      return method.invoke(obj, args);
   }

   public static Object invokeInstanceMethod(Class<?> target, Object obj, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
      Method method = getCachedMethod(target, methodName, parameterTypes);
      method.setAccessible(true);
      return method.invoke(obj, args);
   }

   public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
      Method method = getCachedMethod(clazz, methodName, parameterTypes);
      method.setAccessible(true);
      return method.invoke(null, args);
   }

   private static Field getCachedField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
      Map<String, Field> fieldMap = fieldCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
      return fieldMap.computeIfAbsent(fieldName, k -> {
         try {
            return clazz.getDeclaredField(fieldName);
         } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
         }
      });
   }

   private static Method getCachedMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
      Map<String, Method> methodMap = methodCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
      return methodMap.computeIfAbsent(methodName, k -> {
         try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         }
      });
   }
}
