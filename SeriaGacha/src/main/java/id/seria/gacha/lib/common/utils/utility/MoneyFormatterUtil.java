package id.seria.gacha.lib.common.utils.utility;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MoneyFormatterUtil {
   private static final Map<String, Double> suffixes = new HashMap<>();

   public MoneyFormatterUtil() {
   }

   public static double convertToNumber(String input) {
      String trimmedInput = input.trim().toUpperCase();
      double multiplier = 1.0;

      for (Entry<String, Double> entry : suffixes.entrySet()) {
         String suffix = entry.getKey();
         double value = entry.getValue();
         if (trimmedInput.endsWith(suffix)) {
            multiplier = value;
            trimmedInput = trimmedInput.substring(0, trimmedInput.length() - suffix.length());
            break;
         }
      }

      try {
         return Double.parseDouble(trimmedInput.replace(",", "")) * multiplier;
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Invalid number format: " + input);
      }
   }

   public static String formatMoney(double amount) {
      String[] suffixes = new String[]{"", "K", "M", "B", "T", "qd", "Qn", "sx", "sp", "O", "N"};
      int suffixIndex = 0;

      double reducedAmount;
      for (reducedAmount = amount; reducedAmount >= 1000.0 && suffixIndex < suffixes.length - 1; suffixIndex++) {
         reducedAmount /= 1000.0;
      }

      DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");
      String formattedAmount = decimalFormat.format(reducedAmount);
      return formattedAmount + suffixes[suffixIndex];
   }

   public static void main(String[] args) {
      System.out.println(convertToNumber("1,5B"));
      System.out.println(convertToNumber("100T"));
      System.out.println(convertToNumber("5QD"));
      System.out.println(convertToNumber("2.5QN"));
      System.out.println(convertToNumber("3.75SX"));
      System.out.println(convertToNumber("12SP"));
      System.out.println(convertToNumber("9.99O"));
      System.out.println(convertToNumber("1N"));
   }

   static {
      suffixes.put("K", 1000.0);
      suffixes.put("M", 1000000.0);
      suffixes.put("B", 1.0E9);
      suffixes.put("T", 1.0E12);
      suffixes.put("QD", 1.0E15);
      suffixes.put("QN", 1.0E18);
      suffixes.put("SX", 1.0E21);
      suffixes.put("SP", 1.0E24);
      suffixes.put("O", 1.0E27);
      suffixes.put("N", 1.0E30);
   }
}
