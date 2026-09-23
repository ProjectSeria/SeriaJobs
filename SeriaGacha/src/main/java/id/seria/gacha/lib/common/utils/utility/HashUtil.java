package id.seria.gacha.lib.common.utils.utility;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class HashUtil {
   public HashUtil() {
   }

   public static <T> LinkedHashMap<T, Double> sortByValue(HashMap<T, Double> hm) {
      List<Entry<T, Double>> list = new LinkedList<>(hm.entrySet());
      Collections.sort(list, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
      LinkedHashMap<T, Double> temp = new LinkedHashMap<>();

      for (Entry<T, Double> aa : list) {
         temp.put(aa.getKey(), aa.getValue());
      }

      return temp;
   }
}
