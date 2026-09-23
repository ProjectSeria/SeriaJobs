package id.seria.gacha.lib.common.utils.utility;

public class Validate {
   public Validate() {
   }

   public static void isTrue(boolean condition, String message, Object... arguments) {
      if (!condition) {
         throw new IllegalArgumentException(String.format(message, arguments));
      }
   }
}
