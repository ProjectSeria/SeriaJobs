package id.seria.gacha.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtil {
   public MapUtil() {
   }

   public static <T, V> void addElement(Map<T, List<V>> map, T key, V value) {
      map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
   }

   public static <T, V> void removeElement(Map<T, List<V>> map, T key, V value) {
      List<V> list = map.get(key);
      if (list != null) {
         list.remove(value);
      }
   }
}
