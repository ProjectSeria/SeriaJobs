package id.seria.gacha.lib.common.utils.configuration.handlers;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.ConfigurationUtilities;
import id.seria.gacha.lib.common.utils.configuration.adapter.AdaptersFactory;
import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.logger.BukkitYamlLogger;
import id.seria.gacha.lib.common.utils.configuration.logger.ConfigYamlLogger;
import id.seria.gacha.lib.common.utils.typeresolver.TypeResolver;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import lombok.Generated;
import lombok.NonNull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class ConfigurationWriter {
   private final Configuration configuration;
   private final AdaptersFactory adaptersFactory;
   public ConfigYamlLogger logger = new BukkitYamlLogger();
   private String cachedLoggedPath;

   public ConfigurationWriter(Configuration configuration) {
      this(configuration, new AdaptersFactory());
   }

   public ConfigurationWriter(Configuration configuration, AdaptersFactory adaptersFactory) {
      this.configuration = configuration;
      this.adaptersFactory = adaptersFactory;
   }

   public void writeConfiguration(YamlConfiguration yaml) {
      Set<String> yamlPaths = this.getYamlPaths(yaml);
      Set<String> toClear = new HashSet<>();
      Set<String> toClearPaths = new HashSet<>();
      Map<String, Object> toWrite = this.computeYamlPaths();
      if (toWrite != null) {
         for (String path : yamlPaths) {
            if (!toWrite.containsKey(path)) {
               toClear.add(path);
            }
         }

         Set<String> exceptions = new HashSet<>();

         for (String path : new HashSet<>(toClear)) {
            boolean can = true;

            for (String writePath : toWrite.keySet()) {
               can &= !getParentPath(writePath).equalsIgnoreCase(getParentPath(path));
            }

            if (can) {
               toClearPaths.add(getParentPath(path));
            } else {
               exceptions.add(path);
            }
         }

         for (String path : new HashSet<>(exceptions)) {
            String current = path;

            int index;
            while ((index = current.lastIndexOf(46)) != -1) {
               current = current.substring(0, index);
               if (toClearPaths.contains(current)) {
                  toClearPaths.remove(current);
                  break;
               }
            }
         }

         for (String path : new HashSet<>(toClear)) {
            String current = path;

            int index;
            while ((index = current.lastIndexOf(46)) != -1) {
               current = current.substring(0, index);
               if (toClearPaths.contains(current)) {
                  toClear.remove(path);
                  toClear.add(current);
                  break;
               }
            }
         }

         toClear.forEach(pathx -> yaml.set(pathx, null));
         toWrite.entrySet().forEach(entry -> yaml.set(entry.getKey(), entry.getValue()));
      }
   }

   private static String getParentPath(String path) {
      int index = path.lastIndexOf(46);
      return index == -1 ? path : path.substring(0, index);
   }

   private Set<String> getYamlPaths(YamlConfiguration yaml) {
      Set<String> paths = new HashSet<>();

      for (String path : yaml.getConfigurationSection("").getKeys(true)) {
         if (!yaml.isConfigurationSection(path)) {
            paths.add(path);
         }
      }

      return paths;
   }

   public Map<String, Object> computeYamlPaths() {
      try {
         return this.computeYamlPaths("", this.configuration.getClass(), this.configuration, null, this.configuration);
      } catch (ErrorException | IllegalArgumentException | IllegalAccessException e) {
         this.logger.errorAndFlush(this.cachedLoggedPath, e.getMessage());
         return null;
      }
   }

   public Map<String, Object> computeYamlPaths(String parentPath, @NonNull Type type, @NonNull Object object, ConfigField annotation) throws ErrorException, IllegalArgumentException, IllegalAccessException {
      if (type == null) {
         throw new NullPointerException("type is marked non-null but is null");
      } else if (object == null) {
         throw new NullPointerException("object is marked non-null but is null");
      } else {
         return this.computeYamlPaths(parentPath, type, object, annotation, object);
      }
   }

   private Map<String, Object> computeYamlPaths(String parentPath, @NonNull Type type, @NonNull Object object, ConfigField annotation, Object rootObject) throws ErrorException, IllegalArgumentException, IllegalAccessException {
      if (type == null) {
         throw new NullPointerException("type is marked non-null but is null");
      }

      if (object == null) {
         throw new NullPointerException("object is marked non-null but is null");
      }

      this.cachedLoggedPath = parentPath;
      Object effectiveRootObject = rootObject != null ? rootObject : object;
      Map<String, Object> cachedPaths = new LinkedHashMap<>();
      Class<?> clazz = object.getClass();
      if (List.class.isAssignableFrom(clazz)) {
         if (type instanceof ParameterizedType) {
            Type argumentType = ((ParameterizedType)type).getActualTypeArguments()[0];
            if (annotation != null && annotation.compact() && object instanceof Collection && ((Collection)object).size() == 1) {
               cachedPaths.put(parentPath, this.simplifyObject(((Collection)object).toArray()[0]));
            } else {
               Collection<?> collection = (Collection<?>)object;
               if (collection.isEmpty()) {
                  cachedPaths.put(parentPath, Lists.newArrayList());
               } else {
                  List<String> keys = new ArrayList<>(
                     this.computeYamlPaths(parentPath, argumentType, collection.toArray()[0], annotation, effectiveRootObject).keySet()
                  );
                  if (!(argumentType instanceof ParameterizedType) && keys.size() == 1 && keys.get(0).equals(parentPath)) {
                     List<Object> list = new ArrayList<>();

                     for (Object target : collection) {
                        list.addAll(this.computeYamlPaths(parentPath, argumentType, target, annotation, effectiveRootObject).values());
                     }

                     cachedPaths.put(parentPath, list);
                  } else {
                     int i = 0;

                     for (Object target : collection) {
                        String targetPath = ConfigurationUtilities.joinPath(parentPath, String.valueOf(i++));
                        cachedPaths.putAll(this.computeYamlPaths(targetPath, argumentType, target, annotation, effectiveRootObject));
                     }
                  }
               }
            }
         } else if (annotation != null && annotation.compact() && object instanceof Collection && ((Collection)object).size() == 1) {
            cachedPaths.put(parentPath, this.simplifyObject(((Collection)object).toArray()[0]));
         } else {
            Collection<?> collection = (Collection<?>)object;
            Type argumentType = type instanceof ParameterizedType ? ((ParameterizedType)type).getActualTypeArguments()[0] : this.getType(collection);
            if (collection.isEmpty()) {
               cachedPaths.put(parentPath, Lists.newArrayList());
            } else {
               List<String> keys = new ArrayList<>(
                  this.computeYamlPaths(parentPath, argumentType, collection.toArray()[0], annotation, effectiveRootObject).keySet()
               );
               if (!(argumentType instanceof ParameterizedType) && keys.size() == 1 && keys.get(0).equals(parentPath)) {
                  List<Object> list = new ArrayList<>();

                  for (Object target : collection) {
                     list.addAll(this.computeYamlPaths(parentPath, argumentType, target, annotation, effectiveRootObject).values());
                  }

                  cachedPaths.put(parentPath, list);
               } else {
                  int i = 0;

                  for (Object target : collection) {
                     String targetPath = ConfigurationUtilities.joinPath(parentPath, String.valueOf(i++));
                     cachedPaths.putAll(this.computeYamlPaths(targetPath, argumentType, target, annotation, effectiveRootObject));
                  }
               }
            }
         }
      } else if (Map.class.isAssignableFrom(clazz)) {
         if (type instanceof ParameterizedType) {
            Type keyArgumentType = ((ParameterizedType)type).getActualTypeArguments()[0];
            Type valueArgumentType = ((ParameterizedType)type).getActualTypeArguments()[1];
            if (keyArgumentType instanceof ParameterizedType
               || !(keyArgumentType instanceof Class)
               || !((Class)keyArgumentType).equals(String.class)
                  && !((Class)keyArgumentType).equals(Integer.class)
                  && !Enum.class.isAssignableFrom((Class<?>)keyArgumentType)) {
               throw new ErrorException("Expected \"String\", \"Integer\" or \"Enum\" key for a Map, found \"" + keyArgumentType.getTypeName() + "\"");
            }

            Set<Entry<Object, Object>> entrySet = ((Map)object).entrySet();
            if (entrySet.isEmpty()) {
               cachedPaths.put(parentPath, new YamlConfiguration());
            } else {
               for (Entry<Object, Object> entry : entrySet) {
                  String key = entry.getKey() instanceof Enum ? entry.getKey().toString().toLowerCase() : entry.getKey().toString();
                  String targetPath = ConfigurationUtilities.joinPath(parentPath, key);
                  cachedPaths.putAll(this.computeYamlPaths(targetPath, valueArgumentType, entry.getValue(), annotation, effectiveRootObject));
               }
            }
         } else {
            Set<Entry<Object, Object>> entrySet = ((Map)object).entrySet();
            if (entrySet.isEmpty()) {
               cachedPaths.put(parentPath, new YamlConfiguration());
            } else {
               for (Entry<Object, Object> entry : entrySet) {
                  String key = entry.getKey() instanceof Enum ? entry.getKey().toString().toLowerCase() : entry.getKey().toString();
                  String targetPath = ConfigurationUtilities.joinPath(parentPath, key);
                  cachedPaths.putAll(this.computeYamlPaths(targetPath, entry.getValue().getClass(), entry.getValue(), annotation, effectiveRootObject));
               }
            }
         }
      } else {
         if (this.adaptersFactory.haveAdapter(type)) {
            ConfigAdapter adapter = this.adaptersFactory.getAdapter(type);
            Object adaptedResult = adapter.serialize(new ConfigAdapter.WriterContext<>(this, type, object, effectiveRootObject));
            if (!adaptedResult.equals(object)) {
               Type argument = TypeResolver.reify(ConfigAdapter.class, adapter.getClass());
               Type argumentType = argument instanceof ParameterizedType ? ((ParameterizedType)argument).getActualTypeArguments()[1] : argument;
               cachedPaths.putAll(this.computeYamlPaths(parentPath, argumentType, adaptedResult, annotation, effectiveRootObject));
               return cachedPaths;
            }
         }

         List<ConfigurationUtilities.ConfigFieldHolder> configFields = ConfigurationUtilities.getConfigFieldHolders(object);
         boolean serializable = ConfigurationUtilities.isSerializable(clazz);
         if (configFields.isEmpty()) {
            if (serializable) {
               throw new ErrorException("\"" + clazz.getSimpleName() + "\" is serializable but don't have any configuration fields.");
            }

            cachedPaths.put(parentPath, this.simplifyObject(object, effectiveRootObject));
         } else if (serializable) {
            String serialized = this.serializeObject(ConfigurationUtilities.getSerializableAnnotation(clazz), type, object, effectiveRootObject);
            cachedPaths.put(parentPath, serialized);
         } else {
            for (ConfigurationUtilities.ConfigFieldHolder holder : configFields) {
               Field field = holder.getField();
               field.setAccessible(true);
               String fieldPath = ConfigurationUtilities.joinPath(parentPath, ConfigurationUtilities.extractFieldPath(field));
               Object fieldValue = field.get(holder.getOwner());
               if (fieldValue != null) {
                  cachedPaths.putAll(
                     this.computeYamlPaths(fieldPath, field.getGenericType(), fieldValue, field.getAnnotation(ConfigField.class), effectiveRootObject)
                  );
               }
            }
         }
      }

      return cachedPaths;
   }

   private Class<?> getType(Collection<?> list) {
      return list.isEmpty() ? Object.class : Iterables.getFirst(list, null).getClass();
   }

   public String serializeObject(Serializable serializable, Type type, Object object) throws ErrorException, IllegalArgumentException, IllegalAccessException {
      return this.serializeObject(serializable, type, object, false, false, null);
   }

   public String serializeObject(Serializable serializable, Type type, Object object, Object rootObject) throws ErrorException, IllegalArgumentException, IllegalAccessException {
      return this.serializeObject(serializable, type, object, false, false, rootObject);
   }

   public String serializeObject(Serializable serializable, Type type, Object object, boolean includeSymbols, boolean ignoreAdapters) throws ErrorException, IllegalArgumentException, IllegalAccessException {
      return this.serializeObject(serializable, type, object, includeSymbols, ignoreAdapters, null);
   }

   public String serializeObject(Serializable serializable, Type type, Object object, boolean includeSymbols, boolean ignoreAdapters, Object rootObject) throws ErrorException, IllegalArgumentException, IllegalAccessException {
      Object effectiveRootObject = rootObject != null ? rootObject : object;
      Class<?> clazz = TypeResolver.resolveClass(type);
      if (Map.class.isInstance(object)) {
         throw new ErrorException("Map not allowed for serialization \"" + object.getClass().getSimpleName() + "\"");
      }

      String separator = ConfigurationUtilities.extractSafeSeparator(serializable);
      if (!ignoreAdapters && this.adaptersFactory.haveAdapter(type)) {
         Object adaptedResult = this.adaptersFactory.getAdapter(clazz).serialize(new ConfigAdapter.WriterContext<>(this, type, object, effectiveRootObject));
         return !adaptedResult.equals(object)
            ? this.serializeObject(serializable, adaptedResult.getClass(), adaptedResult, false, false, effectiveRootObject)
            : this.simplifyObject(adaptedResult, effectiveRootObject).toString();
      }

      if (Collection.class.isInstance(object)) {
         Collection<String> listInString = new ArrayList<>();

         for (Object target : (Collection)object) {
            listInString.add(this.serializeObject(serializable, target.getClass(), target, true, false, effectiveRootObject).toString());
         }

         String serializedList = String.join(separator, listInString);
         return includeSymbols ? String.format("[%s]", serializedList) : serializedList;
      } else {
         List<Field> configFields = ConfigurationUtilities.getConfigFields(clazz);
         if (!ConfigurationUtilities.isAbstractClass(clazz) && configFields.size() == 0) {
            return this.simplifyObject(object, effectiveRootObject).toString();
         }

         StringBuilder builder = new StringBuilder();
         if (includeSymbols) {
            builder.append("{");
         }

         if (configFields.size() > 0) {
            for (Field field : configFields) {
               field.setAccessible(true);
               Object fieldValue = field.get(object);
               if (fieldValue != null) {
                  builder.append(this.serializeObject(serializable, field.getGenericType(), fieldValue, true, false, effectiveRootObject));
               }

               builder.append(separator);
            }

            if (builder.length() > 0) {
               builder.setLength(builder.length() - separator.length());
            }
         }

         if (includeSymbols) {
            builder.append("}");
         }

         return builder.toString();
      }
   }

   public Object simplifyObject(Object object) throws ErrorException {
      return this.simplifyObject(object, null);
   }

   public Object simplifyObject(Object object, Object rootObject) throws ErrorException {
      Object effectiveRootObject = rootObject != null ? rootObject : object;
      Class<?> clazz = object.getClass();
      if (this.adaptersFactory.haveAdapter(clazz)) {
         Object adaptedResult = this.adaptersFactory.getAdapter(clazz).serialize(new ConfigAdapter.WriterContext<>(this, clazz, object, effectiveRootObject));
         if (!adaptedResult.equals(object)) {
            return this.simplifyObject(adaptedResult, effectiveRootObject);
         }

         object = adaptedResult;
      }

      if (ItemStack.class.isAssignableFrom(clazz)) {
         return object;
      } else if (List.class.isAssignableFrom(clazz)) {
         return object;
      } else if (Map.class.isAssignableFrom(clazz)) {
         return object;
      } else if (Enum.class.isAssignableFrom(clazz)) {
         return object.toString();
      } else if (ConfigurationUtilities.isPrimitiveType(clazz)) {
         return object;
      } else {
         throw new ErrorException("Class \"" + clazz.getSimpleName() + "\" not allowed, missing config fields or perhaps need an adapter?");
      }
   }

   public void setSilent(boolean state) {
      this.logger.setSilent(state);
   }

   @Generated
   public Configuration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public AdaptersFactory getAdaptersFactory() {
      return this.adaptersFactory;
   }

   @Generated
   public ConfigYamlLogger getLogger() {
      return this.logger;
   }

   @Generated
   public String getCachedLoggedPath() {
      return this.cachedLoggedPath;
   }

   @Generated
   public void setLogger(ConfigYamlLogger logger) {
      this.logger = logger;
   }
}
