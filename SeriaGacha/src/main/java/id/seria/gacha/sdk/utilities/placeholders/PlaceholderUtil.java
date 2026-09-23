package id.seria.gacha.sdk.utilities.placeholders;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.plugin.Plugin;
import id.seria.gacha.sdk.utilities.Colors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Deprecated
public class PlaceholderUtil {
   private static final Map<String, String[]> cache = new HashMap<>();

   public PlaceholderUtil() {
   }

   private static String generateCacheKey(String[] rawMessage, Object[] values) {
      StringBuilder keyBuilder = new StringBuilder();

      for (String element : rawMessage) {
         keyBuilder.append(element).append("|");
      }

      keyBuilder.append("#");

      for (Object value : values) {
         if (value != null && value.getClass().isArray()) {
            keyBuilder.append(arrayToString(value));
         } else {
            keyBuilder.append(value == null ? "null" : value.toString());
         }

         keyBuilder.append("|");
      }

      return keyBuilder.toString();
   }

   private static String arrayToString(Object array) {
      if (array instanceof int[]) {
         return Arrays.toString((int[])array);
      } else if (array instanceof long[]) {
         return Arrays.toString((long[])array);
      } else {
         return array instanceof double[] ? Arrays.toString((double[])array) : Arrays.deepToString((Object[])array);
      }
   }

   public static String[] replacePlaceholders(Plugin plugin, String[] input, Object... keyValuePairs) {
      if (input == null) {
         plugin.getLogger().warn("Tried to replace values from a null message for.");
         return new String[]{"\u00a7cInvalid Message"};
      }

      if (keyValuePairs.length == 0) {
         return input;
      }

      if (keyValuePairs.length % 2 != 0) {
         plugin.getLogger().warn(String.format("Placeholders and replacements do not have matching sizes! %s", Arrays.toString(input)));
         return input;
      }

      String cacheKey = generateCacheKey(input, keyValuePairs);
      if (cache.containsKey(cacheKey)) {
         return cache.get(cacheKey);
      }

      Map<String, Object[]> replacementsMap = new HashMap<>();

      for (int i = 0; i < keyValuePairs.length; i += 2) {
         Object replacement = safeValue(keyValuePairs[i + 1]);
         Object[] replacementArray;
         if (replacement instanceof Collection) {
            replacementArray = ((Collection)replacement).toArray();
         } else if (replacement.getClass().isArray()) {
            replacementArray = Arrays.copyOf((Object[])replacement, ((Object[])replacement).length);
         } else {
            replacementArray = new Object[]{replacement};
         }

         replacementsMap.put(keyValuePairs[i].toString(), replacementArray);
      }

      String[] finalArray = processInputLines(input, replacementsMap);
      cache.put(cacheKey, finalArray);
      return finalArray;
   }

   private static String[] processInputLines(String[] input, Map<String, Object[]> replacementsMap) {
      ArrayList<String> resultList = new ArrayList<>();

      for (String line : input) {
         int maxReplacements = calculateMaxReplacementsForLine(line, replacementsMap);
         if (maxReplacements <= 0) {
            if (!lineRequiresReplacement(line, replacementsMap)) {
               resultList.add(line);
            }
         } else if (!allPlaceholdersEmpty(line, replacementsMap)) {
            for (int i = 0; i < maxReplacements; i++) {
               String modifiedLine = line;

               for (Entry<String, Object[]> entry : replacementsMap.entrySet()) {
                  String key = entry.getKey();
                  Object[] values = entry.getValue();
                  String replacementValue = i < values.length ? toString(values[i]) : "";
                  modifiedLine = modifiedLine.replace(key, replacementValue);
               }

               resultList.add(modifiedLine);
            }
         }
      }

      return resultList.toArray(new String[0]);
   }

   private static String toString(Object object) {
      if (object.getClass().isArray()) {
         return ((Object[])object).length == 1 ? ((Object[])object)[0].toString() : Arrays.toString((Object[])object);
      } else {
         return object.toString();
      }
   }

   private static boolean allPlaceholdersEmpty(String line, Map<String, Object[]> replacementsMap) {
      boolean hasEmptyListPlaceholder = false;

      for (Entry<String, Object[]> entry : replacementsMap.entrySet()) {
         if (line.contains(entry.getKey())) {
            Object[] values = entry.getValue();
            if (values.length != 0) {
               if (values.length != 1 || !"".equals(values[0])) {
                  return false;
               }
            } else {
               hasEmptyListPlaceholder = true;
            }
         }
      }

      return hasEmptyListPlaceholder;
   }

   private static int calculateMaxReplacementsForLine(String line, Map<String, Object[]> replacementsMap) {
      int max = 0;

      for (Entry<String, Object[]> entry : replacementsMap.entrySet()) {
         if (line.contains(entry.getKey())) {
            max = Math.max(max, entry.getValue().length);
         }
      }

      return max;
   }

   private static boolean lineRequiresReplacement(String line, Map<String, Object[]> replacementsMap) {
      for (String key : replacementsMap.keySet()) {
         if (line.contains(key)) {
            return true;
         }
      }

      return false;
   }

   private static Object safeValue(Object value) {
      return value == null ? "null" : value;
   }

   public static List<String> replacePlaceholders(Plugin plugin, List<String> rawMessage, Object... values) {
      return Lists.newArrayList(replacePlaceholders(plugin, rawMessage.toArray(new String[0]), values));
   }

   public static List<String> replacePlaceholdersAndColors(Plugin plugin, List<String> rawMessage, Object... values) {
      String[] message = replacePlaceholders(plugin, rawMessage.toArray(new String[0]), values);
      return Lists.newArrayList(Colors.replaceColorCodes(message));
   }

   public static String[] replacePlaceholdersAndColors(Plugin plugin, String[] rawMessage, Object... values) {
      String[] message = replacePlaceholders(plugin, rawMessage, values);
      return Colors.replaceColorCodes(message);
   }

   public static String replacePlaceholdersAndColors(Plugin plugin, String rawMessage, Object... values) {
      String message = replacePlaceholders(plugin, new String[]{rawMessage}, values)[0];
      return Colors.replaceColorCodes(message);
   }
}
