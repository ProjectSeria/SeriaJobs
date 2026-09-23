package id.seria.gacha.api.crate.animation;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Generated;
import lombok.NonNull;

public class AnimationProperties implements Cloneable {
   private Map<String, Object> properties = new LinkedHashMap<>();

   public static AnimationProperties empty() {
      return new AnimationProperties();
   }

   public static AnimationProperties of(Object... keyValuePairs) {
      if (keyValuePairs.length % 2 != 0) {
         throw new IllegalArgumentException("Arguments must be in key-value pairs.");
      }

      AnimationProperties animationProperties = new AnimationProperties();

      for (int i = 0; i < keyValuePairs.length; i += 2) {
         if (!(keyValuePairs[i] instanceof String)) {
            throw new IllegalArgumentException("Keys must be of type String.");
         }

         animationProperties.addProperty((String)keyValuePairs[i], keyValuePairs[i + 1]);
      }

      return animationProperties;
   }

   private AnimationProperties() {
   }

   public void addProperty(String key, Object value) {
      this.properties.put(key, value);
   }

   public Object getProperty(String key) {
      return this.properties.get(key);
   }

   public <T> T getProperty(String key, Class<T> clazz) {
      Object value = this.properties.get(key);
      if (value == null) {
         return null;
      }

      if (clazz.isInstance(value)) {
         return clazz.cast(value);
      }

      if (value instanceof Number) {
         Number numberValue = (Number)value;
         if (clazz == Integer.class || clazz == int.class) {
            return clazz.cast(numberValue.intValue());
         }

         if (clazz == Long.class || clazz == long.class) {
            return clazz.cast(numberValue.longValue());
         }

         if (clazz == Double.class || clazz == double.class) {
            return clazz.cast(numberValue.doubleValue());
         }

         if (clazz == Float.class || clazz == float.class) {
            return clazz.cast(numberValue.floatValue());
         }
      }

      if (value instanceof String) {
         String strValue = (String)value;
         if (clazz == Integer.class || clazz == int.class) {
            try {
               return clazz.cast(Integer.valueOf(strValue));
            } catch (NumberFormatException var8) {
            }
         }

         if (clazz == Long.class || clazz == long.class) {
            try {
               return clazz.cast(Long.valueOf(strValue));
            } catch (NumberFormatException var7) {
            }
         }

         if (clazz == Double.class || clazz == double.class) {
            try {
               return clazz.cast(Double.valueOf(strValue));
            } catch (NumberFormatException var6) {
            }
         }

         if (clazz == Boolean.class || clazz == boolean.class) {
            return clazz.cast(Boolean.valueOf(strValue));
         }
      }

      if (value instanceof Boolean) {
         Boolean boolValue = (Boolean)value;
         if (clazz == String.class) {
            return clazz.cast(boolValue.toString());
         }

         if (clazz == Integer.class || clazz == int.class) {
            return clazz.cast(boolValue ? 1 : 0);
         }
      }

      return null;
   }

   public <T> T getPropertyOrDefault(String key, @NonNull T defaultValue) {
      if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      }

      Class<T> clazz = (Class<T>)defaultValue.getClass();
      T value = this.getProperty(key, clazz);
      return value != null ? value : defaultValue;
   }

   public Color getColorOrDefault(String key, Color defaultValue) {
      Object value = this.properties.get(key);
      if (value == null) {
         return defaultValue;
      }

      try {
         if (value instanceof Color) {
            return (Color)value;
         }

         if (value instanceof org.bukkit.Color) {
            return new Color(((org.bukkit.Color)value).getRed(), ((org.bukkit.Color)value).getGreen(), ((org.bukkit.Color)value).getBlue());
         }

         if (value instanceof String) {
            String str = ((String)value).trim();
            str = str.replace("#", "");
            if (str.length() == 6) {
               return new Color(Integer.parseInt(str.substring(0, 2), 16), Integer.parseInt(str.substring(2, 4), 16), Integer.parseInt(str.substring(4, 6), 16));
            }

            if (str.length() == 8) {
               return new Color(
                  Integer.parseInt(str.substring(2, 4), 16),
                  Integer.parseInt(str.substring(4, 6), 16),
                  Integer.parseInt(str.substring(6, 8), 16),
                  Integer.parseInt(str.substring(0, 2), 16)
               );
            }
         }
      } catch (Exception var5) {
      }

      return defaultValue;
   }

   public <E extends Enum<E>> E getEnumOrDefault(String key, E defaultValue) {
      Object value = this.properties.get(key);
      if (value == null) {
         return defaultValue;
      }

      try {
         Class<E> enumClass = defaultValue.getDeclaringClass();
         if (enumClass.isInstance(value)) {
            return enumClass.cast(value);
         }

         if (value instanceof String) {
            return Enum.valueOf(enumClass, ((String)value).toUpperCase());
         }

         if (value instanceof Number) {
            int ordinal = ((Number)value).intValue();
            E[] constants = (E[])enumClass.getEnumConstants();
            if (ordinal >= 0 && ordinal < constants.length) {
               return constants[ordinal];
            }
         }
      } catch (Exception var7) {
      }

      return defaultValue;
   }

   public boolean hasProperty(String key) {
      return this.properties.containsKey(key);
   }

   public void removeProperty(String key) {
      this.properties.remove(key);
   }

   public void resetProperties() {
      this.properties.clear();
   }

   public Map<String, Object> map() {
      return this.properties;
   }

   public AnimationProperties clone() {
      AnimationProperties cloned = new AnimationProperties();
      cloned.properties = new HashMap<>(this.properties);
      return cloned;
   }

   @Generated
   public Map<String, Object> getProperties() {
      return this.properties;
   }

   @Generated
   public void setProperties(Map<String, Object> properties) {
      this.properties = properties;
   }
}
