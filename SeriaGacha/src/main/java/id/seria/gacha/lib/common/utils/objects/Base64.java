package id.seria.gacha.lib.common.utils.objects;

import java.util.Base64.Decoder;
import org.apache.commons.lang.Validate;

public class Base64 {
   private final String base64;

   public static boolean isValid(String str) {
      Decoder decoder = java.util.Base64.getDecoder();

      try {
         decoder.decode(str);
         return true;
      } catch (IllegalArgumentException iae) {
         return false;
      }
   }

   public static Base64 fromString(String str) {
      Validate.isTrue(isValid(str), "String is not base64");
      return new Base64(str);
   }

   private Base64(String base64) {
      this.base64 = base64;
   }

   public String decode() {
      return new String(java.util.Base64.getDecoder().decode(this.base64));
   }

   @Override
   public String toString() {
      return this.base64;
   }
}
