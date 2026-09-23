package id.seria.gacha.api.sdk.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lists {
   public Lists() {
   }

   public static <T> List<T> newArrayList() {
      return new ArrayList<>();
   }

   public static <T> List<T> newArrayList(T... elements) {
      return new ArrayList<>(Arrays.asList(elements));
   }

   public static <T> List<T> newArrayList(Iterable<? extends T> elements) {
      List<T> list = new ArrayList<>();
      elements.forEach(list::add);
      return list;
   }
}
