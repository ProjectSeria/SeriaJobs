package id.seria.gacha.sdk.utilities;

import java.util.Collection;

public class Objects {
   public Objects() {
   }

   public static boolean isEmpty(Object obj) {
      if (obj == null) {
         return true;
      } else if (obj instanceof Object[]) {
         return ((Object[])obj).length == 0;
      } else {
         return obj instanceof Collection ? ((Collection)obj).isEmpty() : obj.toString().isEmpty();
      }
   }

   public static boolean isTruthy(String string) {
      if (string == null) {
         return false;
      }

      switch (string) {
         case "t":
         case "true":
         case "on":
         case "y":
         case "yes":
         case "1":
            return true;
         default:
            return false;
      }
   }
}
