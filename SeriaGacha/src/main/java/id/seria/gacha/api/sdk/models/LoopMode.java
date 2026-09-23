package id.seria.gacha.api.sdk.models;

import java.util.Locale;

public enum LoopMode {
   ONCE,
   HOLD,
   LOOP;

   LoopMode() {
   }

   public static LoopMode get(String mode) {
      try {
         return valueOf(mode.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException var2) {
         return ONCE;
      }
   }
}
