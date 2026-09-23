package id.seria.gacha.api.sdk.locales.translatable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public abstract class Translatable implements Cloneable, Translation {
   public Translatable() {
   }

   public static Translatable concatenate(String prefix, Translatable key) {
      return key.postProcessor(str -> prefix + str);
   }

   public static Translatable concatenate(Translatable key, String suffix) {
      return key.postProcessor(str -> str + suffix);
   }

   public static Translatable key(String key, Object... arguments) {
      return new TranslationKey(key, arguments);
   }

   public static Translatable undefined() {
      return new TranslationKey("labels.undefined");
   }

   public static Translatable empty() {
      return new TranslationLiteral(new String[]{""});
   }

   public static Translatable literal(Object value) {
      return new TranslationLiteral(toArray(value));
   }

   public static Translatable literal(String... values) {
      return new TranslationLiteral(Arrays.copyOf(values, values.length));
   }

   public static Translatable label(String value) {
      return new TranslationKey("labels." + value);
   }

   private static String[] toArray(Object value) {
      if (value instanceof String[]) {
         return (String[])value;
      } else {
         return value instanceof List ? ((List<?>)value).stream().map(Object::toString).toArray(String[]::new) : new String[]{value.toString()};
      }
   }

   @Override
   public abstract boolean isEmpty();

   public abstract Translatable postProcessor(Function<String, String> var1);

   public abstract Translatable arguments(Object... var1);

   public abstract Translatable colorless();

   public abstract Translatable lowercase();

   @Override
   public abstract String asString();

   public abstract Translatable clone();
}
