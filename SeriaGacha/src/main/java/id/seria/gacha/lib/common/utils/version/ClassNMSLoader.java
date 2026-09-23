package id.seria.gacha.lib.common.utils.version;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClassNMSLoader {
   private static final Map<String, Constructor<?>> constructorCache = new HashMap<>();

   public ClassNMSLoader() {
   }

   public static <T> T load(Class<T> clazz) {
      return load(clazz, "", new Class[0], new Object[0]);
   }

   public static <T> T load(Class<T> clazz, String path) {
      return load(clazz, path, new Class[0], new Object[0]);
   }

   public static <T> T load(Class<T> clazz, Class<?> parameterType, Object parameter) {
      return load(clazz, "", parameterType, parameter);
   }

   public static <T> T load(Class<T> clazz, String path, Class<?> parameterType, Object parameter) {
      return load(clazz, path, new Class[]{parameterType}, new Object[]{parameter});
   }

   public static <T> T load(Class<T> clazz, Class<?>[] parametersTypes, Object[] parameters) {
      return load(clazz, "", parametersTypes, parameters);
   }

   public static <T> T load(Class<T> clazz, String path, Class<?>[] parametersTypes, Object[] parameters) {
      String key = generateCacheKey(clazz, path, parametersTypes);
      Constructor<?> constructor = constructorCache.get(key);
      if (constructor == null) {
         try {
            constructor = Class.forName(
                  "id.seria.gacha.lib.common.nms."
                     + ServerVersion.getCurrent()
                     + (path.isEmpty() ? "" : "." + path)
                     + "."
                     + clazz.getSimpleName()
                     + "Impl"
               )
               .getConstructor(parametersTypes);
            constructorCache.put(key, constructor);
         } catch (Throwable t) {
            t.printStackTrace();
            return null;
         }
      }

      try {
         return (T)constructor.newInstance(parameters);
      } catch (Throwable t) {
         t.printStackTrace();
         return null;
      }
   }

   private static String generateCacheKey(Class<?> clazz, String path, Class<?>[] parametersTypes) {
      return clazz.getName() + path + Arrays.hashCode(parametersTypes);
   }
}
