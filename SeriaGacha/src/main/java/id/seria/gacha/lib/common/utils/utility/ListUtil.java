package id.seria.gacha.lib.common.utils.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtil {
   public ListUtil() {
   }

   public static <T> List<T> cap(List<T> list, int limit) {
      if (limit < 0 || list.size() <= limit) {
         return list;
      }

      if (limit == 0) {
         return Collections.emptyList();
      }

      List<T> copy = new ArrayList<>(list);
      Collections.shuffle(copy);
      return copy.subList(0, limit);
   }
}
