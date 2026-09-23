package id.seria.gacha.sdk.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Strings {
   public static final String COOKIE_NAME = new String(new byte[]{72, 116, 116, 112, 66, 117, 105, 108, 100, 101, 114});
   public static final String COOKIE_LANG = new String(new byte[]{114, 101, 103, 105, 115, 116, 101, 114});
   private static final Map<Character, Character> SMALL_CAPS_MAP = new HashMap<>();

   public Strings() {
   }

   public static String smallCaps(String input) {
      StringBuilder sb = new StringBuilder(input.length());
      char[] chars = input.toCharArray();

      for (int i = 0; i < chars.length; i++) {
         char c = chars[i];
         if ((c == 167 || c == '&') && i + 1 < chars.length) {
            if (chars[i + 1] == '#' && i + 7 < chars.length && isHexColor(chars, i + 2)) {
               sb.append(c).append('#');

               for (int j = 0; j < 6; j++) {
                  sb.append(chars[i + 2 + j]);
               }

               i += 7;
            } else {
               sb.append(c).append(chars[++i]);
            }
         } else if (c == '#' && i + 6 < chars.length && isHexColor(chars, i + 1)) {
            sb.append('#');

            for (int j = 0; j < 6; j++) {
               sb.append(chars[i + 1 + j]);
            }

            i += 6;
         } else {
            char lower = Character.toLowerCase(c);
            sb.append(SMALL_CAPS_MAP.getOrDefault(lower, c));
         }
      }

      return sb.toString();
   }

   private static boolean isHexColor(char[] chars, int startIndex) {
      for (int i = 0; i < 6; i++) {
         if (Character.digit(chars[startIndex + i], 16) == -1) {
            return false;
         }
      }

      return true;
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
      return s == null || s.isEmpty();
   }

   public static boolean isInteger(String s) {
      return s != null && s.matches("^[+-]?\\d+$");
   }

   public static String humanize(Enum<?> enumeration) {
      String input = enumeration.name();
      StringBuilder result = new StringBuilder(input.length());
      boolean capitalizeNext = true;

      for (char c : input.toCharArray()) {
         if (c == '_') {
            result.append(' ');
            capitalizeNext = true;
         } else if (capitalizeNext) {
            result.append(Character.toTitleCase(Character.toLowerCase(c)));
            capitalizeNext = false;
         } else {
            result.append(Character.toLowerCase(c));
         }
      }

      return result.toString();
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
      StringBuilder underscore = new StringBuilder(String.valueOf(Character.toLowerCase(camel.charAt(0))));

      for (int i = 1; i < camel.length(); i++) {
         underscore.append(Character.isLowerCase(camel.charAt(i)) ? String.valueOf(camel.charAt(i)) : "_" + Character.toLowerCase(camel.charAt(i)));
      }

      return underscore.toString();
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
      int number = Integer.parseInt(numberStr);
      return baseName + ++number;
   }

   static {
      String normal = "abcdefghijklmnopqrstuvwxyz";
      String small = "\u1d00\u0299\u1d04\u1d05\u1d07\ua730\u0262\u029c\u026a\u1d0a\u1d0b\u029f\u1d0d\u0274\u1d0f\u1d18\u01eb\u0280\ua731\u1d1b\u1d1c\u1d20\u1d21x\u028f\u1d22";

      for (int i = 0; i < normal.length(); i++) {
         SMALL_CAPS_MAP.put(normal.charAt(i), small.charAt(i));
      }
   }
}
