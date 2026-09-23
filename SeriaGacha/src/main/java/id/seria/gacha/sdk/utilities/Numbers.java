package id.seria.gacha.sdk.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Numbers {
   public Numbers() {
   }

   public static double round(double value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      }

      BigDecimal bd = new BigDecimal(Double.toString(value));
      bd = bd.setScale(places, RoundingMode.HALF_UP);
      return bd.doubleValue();
   }

   public static float round(float value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      }

      BigDecimal bd = new BigDecimal(Float.toString(value));
      bd = bd.setScale(places, RoundingMode.HALF_UP);
      return bd.floatValue();
   }

   public static double clamp(double val, double min, double max) {
      return Math.max(min, Math.min(max, val));
   }

   public static double clamp(float val, float min, float max) {
      return Math.max(min, Math.min(max, val));
   }

   public static int clamp(int val, int min, int max) {
      return Math.max(min, Math.min(max, val));
   }
}
