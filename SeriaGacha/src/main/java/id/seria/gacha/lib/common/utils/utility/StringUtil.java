package id.seria.gacha.lib.common.utils.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.WordUtils;

public class StringUtil {
   public StringUtil() {
   }

   public static List<String> divideString(String input, int initialLineLength) {
      List<String> result = new ArrayList<>();
      if (input != null && !input.isEmpty()) {
         int lineLength = initialLineLength;
         int index = 0;

         while (index < input.length()) {
            int endIndex = findLineEndIndex(input, index, lineLength);
            result.add(input.substring(index, endIndex).trim());
            index = endIndex + 1;
         }

         while (result.get(result.size() - 1).length() < initialLineLength / 2) {
            result.clear();
            lineLength++;
            index = 0;

            while (index < input.length()) {
               int endIndex = findLineEndIndex(input, index, lineLength);
               result.add(input.substring(index, endIndex).trim());
               index = endIndex + 1;
            }
         }

         return result;
      } else {
         return result;
      }
   }

   private static int findLineEndIndex(String input, int startIndex, int lineLength) {
      int endIndex = startIndex + lineLength;
      if (endIndex >= input.length()) {
         return input.length();
      }

      while (endIndex > startIndex && input.charAt(endIndex) != ' ') {
         endIndex--;
      }

      if (endIndex == startIndex) {
         endIndex = startIndex + lineLength;
      }

      return endIndex;
   }

   public static String camelToFixedString(String str, String fixed) {
      str = trimToEmpty(str);
      if (isEmpty(str)) {
         return str;
      }

      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < str.length(); i++) {
         char c = str.charAt(i);
         if (Character.isUpperCase(c)) {
            if (i != 0) {
               sb.append(fixed);
            }

            sb.append(Character.toLowerCase(c));
         } else {
            sb.append(c);
         }
      }

      return sb.toString();
   }

   public static UUID toUUID(String uuid) {
      String uuidWithHyphens = uuid.replace("-", "")
         .replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
      return UUID.fromString(uuidWithHyphens);
   }

   public static String trimToEmpty(String input) {
      return input == null ? "" : input.trim();
   }

   public static boolean isEmpty(String s) {
      return s == null || s.length() == 0;
   }

   public static boolean isInteger(String s) {
      return s == null ? false : s.matches("^[+-]?\\d+$");
   }

   public static String capitalize(String message) {
      char[] charArray = message.toLowerCase().toCharArray();
      boolean foundSpace = true;

      for (int i = 0; i < charArray.length; i++) {
         if (Character.isLetter(charArray[i])) {
            if (foundSpace) {
               charArray[i] = Character.toUpperCase(charArray[i]);
               foundSpace = false;
            }
         } else {
            foundSpace = true;
         }
      }

      return String.valueOf(charArray);
   }

   public static String camelCaseToUnder(String camel) {
      String underscore = String.valueOf(Character.toLowerCase(camel.charAt(0)));

      for (int i = 1; i < camel.length(); i++) {
         underscore = underscore
            + (Character.isLowerCase(camel.charAt(i)) ? String.valueOf(camel.charAt(i)) : "_" + String.valueOf(Character.toLowerCase(camel.charAt(i))));
      }

      return underscore;
   }

   public static String camelCaseToCapitalize(String camel) {
      return humanize(camelCaseToUnder(camel).replace("_", " "));
   }

   public static String humanizeCamelcase(String str) {
      if (isEmpty(str)) {
         return str;
      }

      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < str.length(); i++) {
         char c = str.charAt(i);
         if (Character.isUpperCase(c)) {
            if (i != 0) {
               sb.append(" ");
            }

            sb.append(c);
         } else {
            sb.append(c);
         }
      }

      return sb.toString();
   }

   public static String humanize(Object object) {
      return WordUtils.capitalize(object.toString().toLowerCase().replace("_", " "));
   }

   public static String humanize(String str) {
      return WordUtils.capitalize(str.toLowerCase().replace("_", " "));
   }

   public static String humanize(Enum<?> enumeration) {
      return WordUtils.capitalize(enumeration.name().toLowerCase().replace("_", " "));
   }

   public static String join(String first, String second) {
      return first.concat(" ").concat(second);
   }

   public static String duplicate(String name) {
      if (!name.matches(".*\\d$")) {
         return name + "1";
      }

      int lastIndex = name.length() - 1;

      while (lastIndex >= 0 && Character.isDigit(name.charAt(lastIndex))) {
         lastIndex--;
      }

      String baseName = name.substring(0, lastIndex + 1);
      String numberStr = name.substring(lastIndex + 1);
      long number = Long.parseLong(numberStr);
      return baseName + (number + 1L);
   }
}
