package id.seria.gacha.lib.common.utils.configuration.handlers;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.ConfigurationTokenizer;
import id.seria.gacha.lib.common.utils.configuration.ConfigurationUtilities;
import id.seria.gacha.lib.common.utils.configuration.adapter.AdaptersFactory;
import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningMissingArgumentException;
import id.seria.gacha.lib.common.utils.configuration.logger.BukkitYamlLogger;
import id.seria.gacha.lib.common.utils.configuration.logger.ConfigYamlLogger;
import id.seria.gacha.lib.common.utils.configuration.parser.ConfigurationFieldParser;
import id.seria.gacha.lib.common.utils.typeresolver.TypeResolver;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class ConfigurationReader {
   private final AdaptersFactory adaptersFactory;
   private ConfigurationFieldParser fieldParser;
   public ConfigYamlLogger logger = new BukkitYamlLogger();
   private String cachedLoggedPath;

   public ConfigurationReader() {
      this(new AdaptersFactory());
   }

   public ConfigurationReader(AdaptersFactory adaptersFactory) {
      this.fieldParser = data -> data;
      this.adaptersFactory = adaptersFactory;
   }

   public <T extends Configuration> T readConfiguration(YamlConfiguration yaml, Class<T> clazz) {
      try {
         return (T)this.toComplexObject(clazz, null, yaml.getConfigurationSection(""), null);
      } catch (WarningMissingArgumentException e) {
         this.logWarning(e);
         if (e.getBuildedObject() instanceof Configuration) {
            return (T)e.getBuildedObject();
         }
      } catch (WarningException e) {
         this.logWarning(e);
      } catch (ErrorException e) {
         this.logger.errorAndFlush(this.cachedLoggedPath, e.getMessage());
      }

      return null;
   }

   public <T> T injectYamlSection(ConfigurationSection section, T object) {
      Class<?> clazz = object.getClass();
      List<ConfigurationUtilities.ConfigFieldHolder> fields = ConfigurationUtilities.getConfigFieldHolders(object);
      if (fields.isEmpty()) {
         this.logger.warningAndFlush(this.cachedLoggedPath, "Expected \"Complex Object\", founded \"" + clazz.getSimpleName() + "\".");
         return object;
      }

      try {
         for (ConfigurationUtilities.ConfigFieldHolder holder : fields) {
            Field field = holder.getField();
            field.setAccessible(true);
            Object fieldValue = field.get(holder.getOwner());
            String fieldPath = ConfigurationUtilities.extractFieldPath(field);
            ConfigField annotation = ConfigurationUtilities.getFieldAnnotation(field);
            List<String> paths = Lists.newArrayList(annotation.legacyPaths());
            paths.add(0, fieldPath);

            for (String relativePath : paths) {
               Object sectionYamlValue = section.get(relativePath);
               if (relativePath.equalsIgnoreCase(fieldPath)) {
                  this.cachedLoggedPath = this.getAbsolutePath(section, relativePath);
               }

               if (sectionYamlValue == null) {
                  if (fieldValue != null) {
                     if (paths.size() == 1) {
                        this.logger
                           .warningAndFlush(this.cachedLoggedPath, "Can't find yaml for \"" + fieldPath + "\". Ignoring yaml value and using predefined value.");
                     } else {
                        this.logger
                           .warningAndFlush(
                              this.cachedLoggedPath,
                              "Can't find yaml for \""
                                 + relativePath
                                 + "\". Looking for legacy paths. "
                                 + (annotation.legacyPaths().length == 0 ? "" : Arrays.toString(annotation.legacyPaths()))
                           );
                     }
                  }
               } else {
                  if (!relativePath.equalsIgnoreCase(fieldPath)) {
                     this.logger.warningAndFlush(this.cachedLoggedPath, "Founded legacy path \"" + relativePath + "\". Using as \"" + fieldPath + "\".");
                  }

                  try {
                     field.set(holder.getOwner(), this.readObject(annotation, field.getGenericType(), fieldValue, sectionYamlValue, object));
                     break;
                  } catch (WarningMissingArgumentException ex) {
                     this.logWarning(ex);
                     if (ex.getBuildedObject() != null) {
                        field.set(holder.getOwner(), ex.getBuildedObject());
                        break;
                     }
                  } catch (WarningException ex) {
                     this.logWarning(ex);
                  }
               }
            }
         }
      } catch (ErrorException | IllegalArgumentException | IllegalAccessException e) {
         this.logger.errorAndFlush(this.cachedLoggedPath, e.getMessage());
      }

      return object;
   }

   private Object readObject(ConfigField configField, @NonNull Type type, Object defaultValue, @NonNull Object yamlValue, Object rootObject) throws WarningException, ErrorException {
      if (type == null) {
         throw new NullPointerException("type is marked non-null but is null");
      }

      if (yamlValue == null) {
         throw new NullPointerException("yamlValue is marked non-null but is null");
      }

      Class<?> clazz = TypeResolver.resolveClass(type);
      if (this.adaptersFactory.haveAdapter(type)) {
         try {
            return this.adaptersFactory.getAdapter(type).deserialize(new ConfigAdapter.ReaderContext(this, type, yamlValue, rootObject));
         } catch (WarningMissingArgumentException e) {
            throw new WarningMissingArgumentException("When using \"" + clazz.getSimpleName() + "\" adapter. " + e.getMessage(), e.getBuildedObject());
         } catch (WarningException e) {
            throw new WarningException("When using \"" + clazz.getSimpleName() + "\" adapter. " + e.getMessage());
         } catch (ErrorException e) {
            throw new ErrorException("When using \"" + clazz.getSimpleName() + "\" adapter. " + e.getMessage());
         }
      } else {
         if (ConfigurationUtilities.containsConfigFields(clazz)) {
            return this.toComplexObject(type, defaultValue, yamlValue, rootObject);
         }

         if (List.class.isAssignableFrom(clazz)) {
            if (!(type instanceof ParameterizedType)) {
               throw new ErrorException("Expected ParameterizedType for class \"" + clazz.getSimpleName() + "\"");
            } else {
               return this.readList(configField, (ParameterizedType)type, yamlValue, rootObject);
            }
         } else if (Map.class.isAssignableFrom(clazz)) {
            if (!(type instanceof ParameterizedType)) {
               throw new ErrorException("Expected ParameterizedType for class \"" + clazz.getSimpleName() + "\"");
            } else {
               return this.readMap(configField, (ParameterizedType)type, yamlValue, rootObject);
            }
         } else if (yamlValue instanceof ConfigurationSection) {
            throw new WarningException("Expected \"" + clazz.getSimpleName() + "\", founded \"Yaml Section\"");
         } else {
            return this.toPrimitiveObject(clazz, yamlValue);
         }
      }
   }

   private List<Object> readList(ConfigField configField, @NonNull ParameterizedType type, @NonNull Object yamlValue, Object rootObject) throws WarningException, ErrorException {
      if (type == null) {
         throw new NullPointerException("type is marked non-null but is null");
      }

      if (yamlValue == null) {
         throw new NullPointerException("yamlValue is marked non-null but is null");
      }

      List<Object> list = new ArrayList<>();
      Type argumentType = type.getActualTypeArguments()[0];
      if (argumentType instanceof ParameterizedType) {
         if (!(yamlValue instanceof ConfigurationSection)) {
            throw new WarningException("Expected \"Yaml Section\", founded \"" + yamlValue.getClass().getSimpleName() + "\"");
         }

         String paramId = "1789460705000296";
         ConfigurationSection section = (ConfigurationSection)yamlValue;
         ParameterizedType paramArgType = (ParameterizedType)argumentType;
         Class<?> paramArgClazz = (Class<?>)paramArgType.getRawType();
         if (List.class.isAssignableFrom(paramArgClazz)) {
            for (String key : section.getKeys(false)) {
               this.cachedLoggedPath = this.getAbsolutePath(section, key);
               Object keyYamlValue = section.get(key);

               try {
                  Object keyObject = this.readList(configField, paramArgType, keyYamlValue, rootObject);
                  Objects.requireNonNull(keyObject);
                  list.add(keyObject);
               } catch (WarningMissingArgumentException ex) {
                  this.logWarning(ex);
                  if (ex.getBuildedObject() != null) {
                     list.add(ex.getBuildedObject());
                  }
               } catch (WarningException ex) {
                  this.logWarning(ex);
               }
            }
         } else {
            if (!Map.class.isAssignableFrom(paramArgClazz)) {
               throw new ErrorException("Expected \"List\" or \"Map\" as ParameterizedType, founded \"" + paramArgClazz.getSimpleName() + "\"");
            }

            for (String key : section.getKeys(false)) {
               this.cachedLoggedPath = this.getAbsolutePath(section, key);
               Object keyYamlValue = section.get(key);

               try {
                  Object keyObject = this.readMap(configField, paramArgType, keyYamlValue, rootObject);
                  Objects.requireNonNull(keyObject);
                  list.add(keyObject);
               } catch (WarningMissingArgumentException ex) {
                  this.logWarning(ex);
                  if (ex.getBuildedObject() != null) {
                     list.add(ex.getBuildedObject());
                  }
               } catch (WarningException ex) {
                  this.logWarning(ex);
               }
            }
         }
      } else if (configField != null && configField.compact() && !(yamlValue instanceof Collection) && !(yamlValue instanceof MemorySection)) {
         if (!(yamlValue instanceof String)) {
            throw new WarningException("Expected \"String\" for compacted list, founded \"" + yamlValue.getClass().getSimpleName() + "\"");
         }

         Object simplifiedObject = this.toPrimitiveObject(argumentType, yamlValue);
         if (!configField.discardEmptyLines() || !(simplifiedObject instanceof String) || ((String)simplifiedObject).length() != 0) {
            list.add(simplifiedObject);
         }
      } else if (yamlValue instanceof ConfigurationSection) {
         ConfigurationSection section = (ConfigurationSection)yamlValue;

         for (String key : section.getKeys(false)) {
            this.cachedLoggedPath = this.getAbsolutePath(section, key);
            Object keyYamlValue = section.get(key);

            try {
               Object keyObject = this.readObject(configField, argumentType, null, keyYamlValue, rootObject);
               Objects.requireNonNull(keyObject);
               if (!configField.discardEmptyLines() || !(keyObject instanceof String) || ((String)keyObject).length() != 0) {
                  list.add(keyObject);
               }
            } catch (WarningMissingArgumentException ex) {
               this.logWarning(ex);
               if (ex.getBuildedObject() != null) {
                  list.add(ex.getBuildedObject());
               }
            } catch (WarningException ex) {
               this.logWarning(ex);
            }
         }
      } else {
         if (!(yamlValue instanceof Collection)) {
            throw new WarningException("Expected \"Yaml Section\" or \"Yaml List\", founded \"" + yamlValue.getClass().getSimpleName() + "\"");
         }

         for (Object object : (Collection)yamlValue) {
            try {
               Object keyObject = this.readObject(configField, argumentType, null, object, rootObject);
               Objects.requireNonNull(keyObject);
               if (!configField.discardEmptyLines() || !(keyObject instanceof String) || ((String)keyObject).length() != 0) {
                  list.add(keyObject);
               }
            } catch (WarningMissingArgumentException ex) {
               this.logWarning(ex);
               if (ex.getBuildedObject() != null) {
                  list.add(ex.getBuildedObject());
               }
            } catch (WarningException ex) {
               this.logWarning(ex);
            }
         }
      }

      return list;
   }

   private String getAbsolutePath(ConfigurationSection section, String relative) {
      return section.getCurrentPath().length() == 0 ? relative : section.getCurrentPath() + "." + relative;
   }

   private Map<Object, Object> readMap(ConfigField configField, @NonNull ParameterizedType type, @NonNull Object yamlValue, Object rootObject) throws ErrorException, WarningException {
      if (type == null) {
         throw new NullPointerException("type is marked non-null but is null");
      }

      if (yamlValue == null) {
         throw new NullPointerException("yamlValue is marked non-null but is null");
      }

      Map<Object, Object> map = new LinkedHashMap<>();
      Type keyArgumentType = type.getActualTypeArguments()[0];
      Type originalValueArgumentType = type.getActualTypeArguments()[1];
      Type valueArgumentType = type.getActualTypeArguments()[1];
      if (!(keyArgumentType instanceof ParameterizedType)
         && keyArgumentType instanceof Class
         && (
            ((Class)keyArgumentType).equals(String.class)
               || ((Class)keyArgumentType).equals(Integer.class)
               || Enum.class.isAssignableFrom((Class<?>)keyArgumentType)
         )) {
         ConfigAdapter adapter = this.adaptersFactory.getAdapter(valueArgumentType);
         if (adapter != null) {
            ParameterizedType adapterType = (ParameterizedType)TypeResolver.reify(ConfigAdapter.class, adapter.getClass());
            Type serializationType = adapterType.getActualTypeArguments()[1];
            valueArgumentType = serializationType;
         }

         if (valueArgumentType instanceof ParameterizedType) {
            if (!(yamlValue instanceof ConfigurationSection)) {
               throw new WarningException("Expected \"Yaml Section\", founded \"" + yamlValue.getClass().getSimpleName() + "\"");
            }

            ConfigurationSection section = (ConfigurationSection)yamlValue;
            ParameterizedType paramArgType = (ParameterizedType)valueArgumentType;
            Class<?> paramArgClazz = (Class<?>)paramArgType.getRawType();
            if (List.class.isAssignableFrom(paramArgClazz)) {
               for (String key : section.getKeys(false)) {
                  this.cachedLoggedPath = this.getAbsolutePath(section, key);
                  Object keyYamlValue = section.get(key);

                  try {
                     Object keyObject = adapter != null
                        ? adapter.deserialize(new ConfigAdapter.ReaderContext(this, valueArgumentType, keyYamlValue, rootObject))
                        : this.readList(configField, paramArgType, keyYamlValue, rootObject);
                     Objects.requireNonNull(keyObject);
                     Class<?> keyArgumentClazz = (Class<?>)keyArgumentType;
                     Object keyForMap;
                     if (Enum.class.isAssignableFrom(keyArgumentClazz)) {
                        Class enumClass = (Class)keyArgumentType;
                        String keyString = key.toUpperCase();
                        keyForMap = Enum.valueOf(enumClass, keyString);
                     } else if (keyArgumentClazz.equals(Integer.class)) {
                        keyForMap = Integer.valueOf(key);
                     } else {
                        keyForMap = key;
                     }

                     map.put(keyForMap, keyObject);
                  } catch (WarningMissingArgumentException ex) {
                     this.logWarning(ex);
                     if (ex.getBuildedObject() != null) {
                        map.put(key, ex.getBuildedObject());
                     }
                  } catch (WarningException ex) {
                     this.logWarning(ex);
                  }
               }
            } else {
               if (!Map.class.isAssignableFrom(paramArgClazz)) {
                  throw new ErrorException("Expected \"List\" or \"Map\" as ParameterizedType, founded \"" + paramArgClazz.getSimpleName() + "\"");
               }

               for (String key : section.getKeys(false)) {
                  this.cachedLoggedPath = this.getAbsolutePath(section, key);
                  Object keyYamlValue = section.get(key);

                  try {
                     Object keyObject = this.readMap(configField, paramArgType, keyYamlValue, rootObject);
                     Objects.requireNonNull(keyObject);
                     Class<?> keyArgumentClazz = (Class<?>)keyArgumentType;
                     Object keyForMap;
                     if (Enum.class.isAssignableFrom(keyArgumentClazz)) {
                        Class enumClass = (Class)keyArgumentType;
                        String keyString = key.toUpperCase();
                        keyForMap = Enum.valueOf(enumClass, keyString);
                     } else if (keyArgumentClazz.equals(Integer.class)) {
                        keyForMap = Integer.valueOf(key);
                     } else {
                        keyForMap = key;
                     }

                     map.put(keyForMap, keyObject);
                  } catch (WarningMissingArgumentException ex) {
                     this.logWarning(ex);
                     if (ex.getBuildedObject() != null) {
                        map.put(key, ex.getBuildedObject());
                     }
                  } catch (WarningException ex) {
                     this.logWarning(ex);
                  }
               }
            }
         } else {
            if (!(yamlValue instanceof ConfigurationSection)) {
               throw new WarningException("Expected \"Yaml Section\", founded \"" + yamlValue.getClass().getSimpleName() + "\"");
            }

            ConfigurationSection section = (ConfigurationSection)yamlValue;

            for (String key : section.getKeys(false)) {
               this.cachedLoggedPath = this.getAbsolutePath(section, key);
               Object keyYamlValue = section.get(key);

               try {
                  Object keyObject = this.readObject(configField, originalValueArgumentType, null, keyYamlValue, rootObject);
                  Objects.requireNonNull(keyObject);
                  Class<?> keyArgumentClazz = (Class<?>)keyArgumentType;
                  Object keyForMap;
                  if (Enum.class.isAssignableFrom(keyArgumentClazz)) {
                     Class enumClass = (Class)keyArgumentType;
                     String keyString = key.toUpperCase();
                     keyForMap = Enum.valueOf(enumClass, keyString);
                  } else if (keyArgumentClazz.equals(Integer.class)) {
                     keyForMap = Integer.valueOf(key);
                  } else {
                     keyForMap = key;
                  }

                  map.put(keyForMap, keyObject);
               } catch (WarningMissingArgumentException ex) {
                  this.logWarning(ex);
                  if (ex.getBuildedObject() != null) {
                     map.put(key, ex.getBuildedObject());
                  }
               } catch (WarningException ex) {
                  this.logWarning(ex);
               }
            }
         }

         return map;
      } else {
         throw new ErrorException("Expected \"String\", \"Integer\" or \"Enum\" key for a Map, founded \"" + keyArgumentType.getTypeName() + "\"");
      }
   }

   public Object toComplexObject(Type type, Object defaultValue, Object yamlValue) throws WarningException, ErrorException {
      return this.toComplexObject(type, defaultValue, yamlValue, null);
   }

   public Object toComplexObject(Type type, Object defaultValue, Object yamlValue, Object rootObject) throws WarningException, ErrorException {
      if (type instanceof ParameterizedType) {
         throw new ErrorException("Expected \"Variable Type\", founded \"" + type.getTypeName() + "\"");
      }

      Class<?> clazz = (Class<?>)type;

      try {
         if (ConfigurationUtilities.isSerializable(clazz)) {
            if (!(yamlValue instanceof String)) {
               throw new WarningException("Expected \"String\" for serialization, founded \"" + yamlValue.getClass().getSimpleName() + "\"");
            }

            Serializable serializableAnnotation = ConfigurationUtilities.getSerializableAnnotation(clazz);
            return this.unserializeObject(
               serializableAnnotation,
               type,
               new ConfigurationTokenizer((String)yamlValue, ConfigurationUtilities.extractSafeSeparator(serializableAnnotation)),
               rootObject
            );
         } else {
            Object instance = defaultValue == null ? clazz.getConstructor().newInstance() : defaultValue;
            Object effectiveRootObject = rootObject != null ? rootObject : instance;
            List<ConfigurationUtilities.ConfigFieldHolder> fields = ConfigurationUtilities.getConfigFieldHolders(instance);
            if (fields.size() == 0) {
               throw new ErrorException("Expected \"Complex Object\", founded \"" + yamlValue.getClass().getSimpleName() + "\"");
            }

            if (!(yamlValue instanceof ConfigurationSection)) {
               throw new WarningException("Expected \"Yaml Section\" for \"Complex Object\", founded \"" + yamlValue.getClass().getSimpleName() + "\"");
            }

            ConfigurationSection section = (ConfigurationSection)yamlValue;

            for (ConfigurationUtilities.ConfigFieldHolder holder : fields) {
               Field field = holder.getField();
               field.setAccessible(true);
               Object fieldValue = field.get(holder.getOwner());
               String fieldPath = ConfigurationUtilities.extractFieldPath(field);
               ConfigField annotation = ConfigurationUtilities.getFieldAnnotation(field);
               List<String> paths = Lists.newArrayList(annotation.legacyPaths());
               paths.add(0, fieldPath);

               for (String relativePath : paths) {
                  Object sectionYamlValue = section.get(relativePath);
                  if (relativePath.equalsIgnoreCase(fieldPath)) {
                     this.cachedLoggedPath = this.getAbsolutePath(section, relativePath);
                  }

                  if (sectionYamlValue == null) {
                     if (fieldValue != null) {
                        if (paths.size() == 1) {
                           this.logger
                              .warningAndFlush(
                                 this.cachedLoggedPath, "Can't find yaml for \"" + fieldPath + "\". Ignoring yaml value and using predefined value."
                              );
                        } else {
                           this.logger
                              .warningAndFlush(
                                 this.cachedLoggedPath,
                                 "Can't find yaml for \""
                                    + relativePath
                                    + "\". Looking for legacy paths. "
                                    + (annotation.legacyPaths().length == 0 ? "" : Arrays.toString(annotation.legacyPaths()))
                              );
                        }
                     }
                  } else {
                     if (!relativePath.equalsIgnoreCase(fieldPath)) {
                        this.logger.warningAndFlush(this.cachedLoggedPath, "Founded legacy path \"" + relativePath + "\". Using as \"" + fieldPath + "\".");
                     }

                     try {
                        field.set(holder.getOwner(), this.readObject(annotation, field.getGenericType(), fieldValue, sectionYamlValue, effectiveRootObject));
                        break;
                     } catch (WarningMissingArgumentException ex) {
                        this.logWarning(ex);
                        if (ex.getBuildedObject() != null) {
                           field.set(holder.getOwner(), ex.getBuildedObject());
                           break;
                        }
                     } catch (WarningException ex) {
                        this.logWarning(ex);
                     }
                  }
               }
            }

            return instance;
         }
      } catch (InvocationTargetException | InstantiationException | NoSuchMethodException e) {
         throw new ErrorException("No default constructor was found for \"" + clazz.getSimpleName() + "\"");
      } catch (IllegalArgumentException | IllegalAccessException e) {
         throw new ErrorException(e.getMessage());
      }
   }

   public Object unserializeObject(Serializable serializableAnnotation, Type type, ConfigurationTokenizer tokenizer) throws WarningException, ErrorException {
      return this.unserializeObject(serializableAnnotation, type, tokenizer, null);
   }

   public Object unserializeObject(Serializable serializableAnnotation, Type type, ConfigurationTokenizer tokenizer, Object rootObject) throws WarningException, ErrorException {
      Class<?> clazz = (Class<?>)type;
      List<Field> fields = ConfigurationUtilities.getConfigFields(clazz);
      if (fields.size() == 0) {
         return this.toPrimitiveObject(type, tokenizer.getStr());
      }

      try {
         Object instance = clazz.getConstructor().newInstance();
         Object effectiveRootObject = rootObject != null ? rootObject : instance;
         int i = 0;

         for (Field field : fields) {
            if (ConfigurationUtilities.isConfigField(field)) {
               field.setAccessible(true);
               if (!tokenizer.hasToken()) {
                  throw new WarningMissingArgumentException("Expected " + fields.size() + " arguments, founded " + i + " argument(s)", instance);
               }

               if (!(field.getGenericType() instanceof ParameterizedType)) {
                  try {
                     ConfigAdapter adapter = this.adaptersFactory.getAdapter(field.getGenericType());
                     if (adapter != null) {
                        ParameterizedType adapterType = (ParameterizedType)TypeResolver.reify(ConfigAdapter.class, adapter.getClass());
                        Type serializationType = adapterType.getActualTypeArguments()[1];
                        if (ConfigurationUtilities.containsConfigFields(serializationType)) {
                           field.set(
                              instance,
                              adapter.deserialize(
                                 new ConfigAdapter.ReaderContext(
                                    this,
                                    serializationType,
                                    this.unserializeObject(
                                       serializableAnnotation,
                                       serializationType,
                                       new ConfigurationTokenizer(tokenizer.nextTokenAsObject(), tokenizer.getSeparator()),
                                       effectiveRootObject
                                    ),
                                    effectiveRootObject
                                 )
                              )
                           );
                        } else {
                           field.set(
                              instance,
                              adapter.deserialize(
                                 new ConfigAdapter.ReaderContext(
                                    this,
                                    serializationType,
                                    this.unserializeObject(
                                       serializableAnnotation,
                                       serializationType,
                                       new ConfigurationTokenizer(tokenizer.nextToken(), tokenizer.getSeparator()),
                                       effectiveRootObject
                                    ),
                                    effectiveRootObject
                                 )
                              )
                           );
                        }
                     } else if (ConfigurationUtilities.containsConfigFields(field.getGenericType())) {
                        field.set(
                           instance,
                           this.unserializeObject(
                              serializableAnnotation,
                              field.getGenericType(),
                              new ConfigurationTokenizer(tokenizer.nextTokenAsObject(), tokenizer.getSeparator()),
                              effectiveRootObject
                           )
                        );
                     } else {
                        field.set(instance, this.toPrimitiveObject(field.getGenericType(), tokenizer.nextToken()));
                     }
                  } catch (WarningMissingArgumentException ex) {
                     this.logWarning(ex);
                     if (ex.getBuildedObject() != null) {
                        field.set(instance, ex.getBuildedObject());
                     }
                  } catch (WarningException ex) {
                     this.logWarning(ex);
                  }
               } else {
                  ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
                  Type argumentType = parameterizedType.getActualTypeArguments()[0];
                  if (argumentType instanceof ParameterizedType) {
                     throw new ErrorException("Expected \"Variable Type\", founded \"Parameterized Type\"");
                  }

                  if (argumentType instanceof ParameterizedType && !List.class.isAssignableFrom((Class<?>)parameterizedType.getRawType())) {
                     throw new ErrorException("Only allowed \"List\", founded \"" + parameterizedType.getRawType().getClass().getSimpleName() + "\"");
                  }

                  ConfigAdapter adapter = this.adaptersFactory.getAdapter(argumentType);
                  List<Object> list = new ArrayList<>();
                  ConfigurationTokenizer arrayTokenizer = new ConfigurationTokenizer(tokenizer.nextTokenAsArray(), tokenizer.getSeparator());

                  while (arrayTokenizer.hasToken()) {
                     try {
                        if (adapter != null) {
                           ParameterizedType adapterType = (ParameterizedType)TypeResolver.reify(ConfigAdapter.class, adapter.getClass());
                           Type serializationType = adapterType.getActualTypeArguments()[1];
                           list.add(
                              adapter.deserialize(
                                 new ConfigAdapter.ReaderContext(
                                    this,
                                    serializationType,
                                    this.unserializeObject(
                                       serializableAnnotation,
                                       serializationType,
                                       new ConfigurationTokenizer(arrayTokenizer.nextToken(), tokenizer.getSeparator()),
                                       effectiveRootObject
                                    ),
                                    effectiveRootObject
                                 )
                              )
                           );
                        } else if (ConfigurationUtilities.containsConfigFields(argumentType)) {
                           list.add(
                              this.unserializeObject(
                                 serializableAnnotation,
                                 argumentType,
                                 new ConfigurationTokenizer(arrayTokenizer.nextTokenAsObject(), tokenizer.getSeparator()),
                                 effectiveRootObject
                              )
                           );
                        } else {
                           list.add(this.toPrimitiveObject(argumentType, arrayTokenizer.nextToken()));
                        }
                     } catch (WarningMissingArgumentException ex) {
                        this.logWarning(ex);
                        if (ex.getBuildedObject() != null) {
                           list.add(ex.getBuildedObject());
                        }
                     } catch (WarningException ex) {
                        this.logWarning(ex);
                     }
                  }

                  field.set(instance, list);
               }

               i++;
            }
         }

         return instance;
      } catch (InvocationTargetException | InstantiationException | NoSuchMethodException e) {
         throw new ErrorException("No default constructor was found for \"" + clazz.getSimpleName() + "\"");
      } catch (IllegalArgumentException | IllegalAccessException e) {
         throw new ErrorException(e.getMessage());
      }
   }

   public Object toPrimitiveObject(Type type, Object object) throws ErrorException, WarningException {
      if (type instanceof ParameterizedType) {
         throw new ErrorException("Expected \"Variable Type\", founded \"Parameterized Type\"");
      }

      Class<?> clazz = TypeResolver.resolveClass(type);
      if (ConfigurationUtilities.containsConfigFields(clazz)) {
         throw new ErrorException("Can't convert \"Complex Object\" to \"Primitive Type\"");
      }

      if (Enum.class.isAssignableFrom(clazz)) {
         if (!(object instanceof String)) {
            throw new WarningException("Expected \"String\" for \"Enum\", founded \"" + clazz.getSimpleName() + "\"");
         } else {
            return this.fieldParser.parse(Enum.valueOf((Class)clazz, object.toString()));
         }
      } else {
         if (ItemStack.class.isAssignableFrom(clazz)) {
            return this.fieldParser.parse(object);
         }

         try {
            if (Boolean.class.isAssignableFrom(clazz)) {
               return this.fieldParser.parse(Boolean.parseBoolean(object.toString()));
            }

            if (Integer.class.isAssignableFrom(clazz)) {
               if (object instanceof Double) {
                  return this.fieldParser.parse((int)Double.parseDouble(object.toString()));
               }

               return this.fieldParser.parse(Integer.parseInt(object.toString()));
            }

            if (Long.class.isAssignableFrom(clazz)) {
               return this.fieldParser.parse(Long.parseLong(object.toString()));
            }

            if (BigInteger.class.isAssignableFrom(clazz)) {
               return this.fieldParser.parse(new BigInteger(object.toString()));
            }

            if (Double.class.isAssignableFrom(clazz)) {
               return this.fieldParser.parse(Double.parseDouble(object.toString()));
            }

            if (Short.class.isAssignableFrom(clazz)) {
               return this.fieldParser.parse(Short.parseShort(object.toString()));
            }

            if (Float.class.isAssignableFrom(clazz)) {
               return this.fieldParser.parse(Float.parseFloat(object.toString()));
            }

            if (String.class.isAssignableFrom(clazz)) {
               return this.fieldParser.parse(object.toString());
            }
         } catch (Exception e) {
            throw new WarningException("Expected \"" + clazz.getSimpleName() + "\", founded \"" + object.getClass().getSimpleName() + "\"");
         }

         throw new ErrorException("Unknown object class \"" + clazz.getSimpleName() + "\", perhaps don't have any config fields or any adapter?");
      }
   }

   private void logWarning(WarningException ex) {
      if (ex instanceof WarningMissingArgumentException) {
         this.logger.warningAndFlush(this.cachedLoggedPath, ex.getMessage() + ". Filling the rest with predefined value.");
      } else {
         this.logger.warningAndFlush(this.cachedLoggedPath, ex.getMessage() + ". Ignoring yaml value and using predefined value.");
      }
   }

   public void setSilent(boolean state) {
      this.logger.setSilent(state);
   }

   @Generated
   public AdaptersFactory getAdaptersFactory() {
      return this.adaptersFactory;
   }

   @Generated
   public ConfigurationFieldParser getFieldParser() {
      return this.fieldParser;
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
   public void setFieldParser(ConfigurationFieldParser fieldParser) {
      this.fieldParser = fieldParser;
   }

   @Generated
   public void setLogger(ConfigYamlLogger logger) {
      this.logger = logger;
   }
}
