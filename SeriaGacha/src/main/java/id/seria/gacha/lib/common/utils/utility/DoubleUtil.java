package id.seria.gacha.lib.common.utils.utility;

import java.text.DecimalFormat;

public class DoubleUtil {
   private static final DecimalFormat FORMAT = new DecimalFormat("#.#");

   public DoubleUtil() {
   }

   public static double fix(double value) {
      return Math.abs(value) < 1.0E-6 ? 0.0 : value;
   }

   public static String format(double value) {
      return FORMAT.format(value);
   }
}
