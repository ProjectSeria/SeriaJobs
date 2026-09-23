package id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectModifiers {
   public static ObjectValueModifier<Number> POSITIVE_NUMBER = value -> value.doubleValue() < 0.0 ? 0 : value;
   public static ObjectValueModifier<Number> GREATHER_THEN_ONE = value -> value.doubleValue() < 1.0 ? 1 : value;
   public static ObjectValueModifier<Number> ALWAYS_ODD_NUMBER = value -> value.intValue() % 2 == 0 ? value.intValue() + 1 : value;
   public static ObjectValueModifier<String> ID_NORMALIZER = value -> {
      StringBuilder ret = new StringBuilder();
      Matcher matches = Pattern.compile("[A-Za-z0-9_]+").matcher(value.replace("-", "_"));

      while (matches.find()) {
         ret.append(matches.group());
      }

      return ret.toString();
   };

   public ObjectModifiers() {
   }

   public static ObjectValueModifier<Number> CLAMP(Number min, Number max) {
      return value -> {
         if (value.doubleValue() < min.doubleValue()) {
            return min;
         } else {
            return value.doubleValue() > max.doubleValue() ? max : value;
         }
      };
   }

   public static ObjectValueModifier<Number> GREATHER_THEN(Number min) {
      return value -> value.doubleValue() < min.doubleValue() ? min : value;
   }

   public static ObjectValueModifier<String> ID_NORMALIZER(boolean allowDots) {
      return value -> {
         StringBuilder ret = new StringBuilder();
         Matcher matches = Pattern.compile(allowDots ? "[A-Za-z0-9_\\.]+" : "[A-Za-z0-9_]+").matcher(value.replace("-", "_"));

         while (matches.find()) {
            ret.append(matches.group());
         }

         return ret.toString();
      };
   }
}
