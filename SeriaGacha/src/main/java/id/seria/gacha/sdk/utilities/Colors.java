package id.seria.gacha.sdk.utilities;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class Colors {
   private static final boolean[] COLOR_CODES = new boolean[128];
   public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('\u00a7') + "[0-9A-FK-OR]");

   public Colors() {
   }

   public static String replaceColorCodes(String rawMessage) {
      int length = rawMessage.length();
      char[] chars = rawMessage.toCharArray();
      StringBuilder translated = new StringBuilder(length);

      for (int i = 0; i < length; i++) {
         char current = chars[i];
         if (current == '&' && i + 7 < length && chars[i + 1] == '#') {
            translated.append("\u00a7x");
            boolean isValidHex = true;

            for (int j = 0; j < 6; j++) {
               char hexChar = chars[i + 2 + j];
               if ((hexChar < '0' || hexChar > '9') && (hexChar < 'a' || hexChar > 'f') && (hexChar < 'A' || hexChar > 'F')) {
                  isValidHex = false;
                  break;
               }

               translated.append('\u00a7').append(hexChar);
            }

            if (isValidHex) {
               i += 7;
               continue;
            }

            translated.setLength(i);
         }

         if (current == '&' && i + 1 < length) {
            char next = chars[i + 1];
            if (next < 128 && COLOR_CODES[next]) {
               char lowerNext = next >= 'A' && next <= 'Z' ? (char)(next | 32) : next;
               translated.append('\u00a7').append(lowerNext);
               i++;
               continue;
            }
         }

         translated.append(current);
      }

      return translated.toString();
   }

   public static String[] replaceColorCodes(String... rawMessage) {
      String[] copy = Arrays.copyOf(rawMessage, rawMessage.length);

      for (int i = 0; i < copy.length; i++) {
         copy[i] = replaceColorCodes(copy[i]);
      }

      return copy;
   }

   public static String stripColor(String input) {
      return input == null ? null : STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
   }

   static {
      String validCodes = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

      for (char c : validCodes.toCharArray()) {
         COLOR_CODES[c] = true;
      }
   }
}
