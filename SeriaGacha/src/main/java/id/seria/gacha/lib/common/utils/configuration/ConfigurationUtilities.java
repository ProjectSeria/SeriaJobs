package id.seria.gacha.lib.common.utils.configuration;

import com.google.common.primitives.Primitives;
import id.seria.gacha.lib.common.utils.configuration.annotations.After;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.LoadFirst;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.Validate;

public class ConfigurationUtilities {
   public static final Serializable GENERIC_SERIALIZATION = new Serializable() {
      @Override
      public Class<? extends Annotation> annotationType() {
         return Serializable.class;
      }

      @Override
      public String separator() {
         return ";";
      }
   };

   private ConfigurationUtilities() {
   }

   public static boolean isConfigField(Field field) {
      return field.getAnnotation(ConfigField.class) != null;
   }

   public static ConfigField getFieldAnnotation(Field field) {
      Validate.isTrue(isConfigField(field), field.toString() + " is not a config field.");
      return field.getAnnotation(ConfigField.class);
   }

   public static boolean isSerializable(Type type) {
      return type instanceof Class && isSerializable((Class<?>)type);
   }

   public static boolean isSerializable(Class<?> clazz) {
      return clazz.getAnnotation(Serializable.class) != null;
   }

   public static Serializable getSerializableAnnotation(Class<?> clazz) {
      Validate.isTrue(isSerializable(clazz), clazz.getSimpleName() + " is not serializable.");
      return clazz.getAnnotation(Serializable.class);
   }

   public static boolean isPrimitiveType(Class<?> clazz) {
      return Primitives.isWrapperType(Primitives.wrap(clazz)) || clazz.equals(String.class);
   }

   public static String extractFieldPath(Field field) {
      ConfigField annotation = getFieldAnnotation(field);
      return annotation.path().trim().length() == 0 ? normalizePath(field.getName()) : annotation.path();
   }

   public static String extractSafeSeparator(Serializable serializable) throws ErrorException {
      String separator = serializable.separator();
      if (separator.contains(",")) {
         throw new ErrorException("Separator \"" + separator + "\" not allowed for serialization");
      } else {
         return separator;
      }
   }

   public static List<Field> getConfigFields(Class<?> clazz) {
      List<Class<?>> classes = new ArrayList<>();

      for (Class<?> target = clazz; target.getSuperclass() != null; target = target.getSuperclass()) {
         classes.add(target);
      }

      Collections.reverse(classes);
      List<Field> loadFirstFields = new ArrayList<>();
      List<Field> regularFields = new ArrayList<>();

      for (Class<?> target : classes) {
         for (Field field : target.getDeclaredFields()) {
            if (isConfigField(field)) {
               if (field.getAnnotation(LoadFirst.class) != null) {
                  loadFirstFields.add(field);
               } else {
                  regularFields.add(field);
               }
            }
         }
      }

      List<Field> fields = new ArrayList<>(loadFirstFields.size() + regularFields.size());
      fields.addAll(loadFirstFields);
      fields.addAll(regularFields);
      return fields;
   }

   public static List<ConfigurationUtilities.ConfigFieldHolder> getConfigFieldHolders(Object object) {
      List<ConfigurationUtilities.ConfigFieldHolder> fields = new ArrayList<>();

      for (Field field : getConfigFields(object.getClass())) {
         fields.add(new ConfigurationUtilities.ConfigFieldHolder(object, field));
      }

      if (object instanceof Configuration) {
         Configuration configuration = (Configuration)object;

         for (Configuration module : configuration.getRegisteredModules()) {
            for (Field field : getConfigFields(module.getClass())) {
               fields.add(new ConfigurationUtilities.ConfigFieldHolder(module, field));
            }
         }
      }

      for (int i = 0; i < fields.size(); i++) {
         ConfigurationUtilities.ConfigFieldHolder holder = fields.get(i);
         After after = holder.getField().getAnnotation(After.class);
         if (after != null && after.value().trim().length() != 0) {
            int targetIndex = findLastAnchorIndex(fields, after.value(), holder);
            if (targetIndex != -1) {
               fields.remove(i);
               int insertAt = targetIndex + 1;
               if (i < insertAt) {
                  insertAt--;
               }

               fields.add(Math.min(insertAt, fields.size()), holder);
            }
         }
      }

      return fields;
   }

   public static int countConfigFields(Class<?> clazz) {
      int amount = 0;

      for (Class<?> target = clazz; target.getSuperclass() != null; target = target.getSuperclass()) {
         for (Field field : target.getDeclaredFields()) {
            if (isConfigField(field)) {
               amount++;
            }
         }
      }

      return amount;
   }

   public static boolean containsConfigFields(Type type) {
      return type instanceof Class && containsConfigFields((Class<?>)type);
   }

   public static boolean containsConfigFields(Class<?> clazz) {
      return countConfigFields(clazz) > 0;
   }

   public static String joinPath(String first, String second) {
      if (first == null || first.length() == 0) {
         return second;
      } else {
         return second != null && second.length() != 0 ? first + "." + second : first;
      }
   }

   public static String normalizePath(String path) {
      return StringUtil.camelToFixedString(path, "-");
   }

   public static boolean isAbstractClass(Class<?> clazz) {
      if (!clazz.isEnum() && !Primitives.unwrap(clazz).isPrimitive() && !clazz.equals(String.class)) {
         boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
         return clazz.getSuperclass() != null && isAbstractClass(clazz.getSuperclass()) ? true : isAbstract;
      } else {
         return false;
      }
   }

   public static boolean matchesAnchor(Field field, String anchor) {
      if (field != null && anchor != null) {
         String normalized = anchor.trim();
         if (normalized.length() == 0) {
            return false;
         }

         String fieldName = field.getName();
         String fieldPath = extractFieldPath(field);
         return fieldName.equalsIgnoreCase(normalized) || fieldPath.equalsIgnoreCase(normalized);
      } else {
         return false;
      }
   }

   public static int findLastAnchorIndex(List<Field> fields, String anchor) {
      return findLastAnchorIndex(fields, anchor, (Field)null);
   }

   public static int findLastAnchorIndex(List<Field> fields, String anchor, Field ignore) {
      if (fields != null && anchor != null) {
         String normalized = anchor.trim();
         if (normalized.length() == 0) {
            return -1;
         }

         int index = -1;

         for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            if ((ignore == null || field != ignore) && matchesAnchor(field, normalized)) {
               index = i;
            }
         }

         return index;
      } else {
         return -1;
      }
   }

   private static int findLastAnchorIndex(List<ConfigurationUtilities.ConfigFieldHolder> fields, String anchor, ConfigurationUtilities.ConfigFieldHolder ignore) {
      Field ignoreField = ignore != null ? ignore.getField() : null;
      return findLastAnchorIndex(extractFields(fields), anchor, ignoreField);
   }

   private static List<Field> extractFields(List<ConfigurationUtilities.ConfigFieldHolder> holders) {
      List<Field> fields = new ArrayList<>(holders.size());

      for (ConfigurationUtilities.ConfigFieldHolder holder : holders) {
         fields.add(holder.getField());
      }

      return fields;
   }

   public static final class ConfigFieldHolder {
      private final Object owner;
      private final Field field;

      public ConfigFieldHolder(Object owner, Field field) {
         this.owner = owner;
         this.field = field;
      }

      public Object getOwner() {
         return this.owner;
      }

      public Field getField() {
         return this.field;
      }
   }
}
