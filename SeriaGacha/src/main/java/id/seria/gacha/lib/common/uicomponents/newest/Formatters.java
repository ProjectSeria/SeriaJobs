package id.seria.gacha.lib.common.uicomponents.newest;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import java.util.function.Function;

public class Formatters {
   public static Function<Number, Translatable> SECONDS = value -> {
      if (value.doubleValue() == value.intValue()) {
         return value.intValue() == 1
            ? Translatable.literal(String.format("%d second", value.intValue()))
            : Translatable.literal(String.format("%d seconds", value.intValue()));
      } else {
         return value.doubleValue() == 1.0
            ? Translatable.literal(String.format("%.1f second", value.doubleValue()))
            : Translatable.literal(String.format("%.1f seconds", value.doubleValue()));
      }
   };
   public static Function<Number, Translatable> PERCENTAGE = value -> value.doubleValue() == value.intValue()
      ? Translatable.literal(String.format("%d%%", value.intValue()))
      : Translatable.literal(String.format("%.2f%%", value.doubleValue()));

   public Formatters() {
   }
}
