package id.seria.gacha.sdk.utilities;

import java.math.BigDecimal;

public class Parsers {
   public Parsers() {
   }

   public static Double parseDouble(String var) {
      return parseDouble(var, null);
   }

   public static Double parseDouble(String var, Double def) {
      if (var == null) {
         return def;
      }

      try {
         return Double.parseDouble(var);
      } catch (NumberFormatException var3) {
         return def;
      }
   }

   public static Float parseFloat(String var) {
      return parseFloat(var, null);
   }

   public static Float parseFloat(String var, Float def) {
      if (var == null) {
         return def;
      }

      try {
         return Float.parseFloat(var);
      } catch (NumberFormatException var3) {
         return def;
      }
   }

   public static Long parseLong(String var) {
      return parseLong(var, null);
   }

   public static Long parseLong(String var, Long def) {
      if (var == null) {
         return def;
      }

      try {
         return Long.parseLong(var);
      } catch (NumberFormatException var3) {
         return def;
      }
   }

   public static Integer parseInt(String var) {
      return parseInt(var, null);
   }

   public static Integer parseInt(String var, Integer def) {
      if (var == null) {
         return def;
      }

      try {
         return Integer.parseInt(var);
      } catch (NumberFormatException var3) {
         return def;
      }
   }

   public static Number parseNumber(String num) {
      return Double.parseDouble(num);
   }

   public static BigDecimal parseBigNumber(String num) {
      return new BigDecimal(num);
   }
}
