package id.seria.gacha.lib.common.modelengine.interfaces.enums;

import java.util.Locale;

public enum MegLoopMode {
   ONCE,
   HOLD,
   LOOP;

   MegLoopMode() {
   }

   public static MegLoopMode get(String mode) {
      try {
         return valueOf(mode.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException var2) {
         return ONCE;
      }
   }
}
