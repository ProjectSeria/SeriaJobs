package id.seria.gacha.sdk.core.internal;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Filters {
   public Filters() {
   }

   public static List<String> filter(Collection<String> inputs, String filter) {
      return filter(inputs.stream(), filter);
   }

   public static List<String> filter(Stream<String> stream, String filter) {
      if (!filter.isEmpty()) {
         stream = stream.filter(name -> name.toLowerCase().startsWith(filter.toLowerCase()))
            .sorted(Comparator.comparing(name -> name.toLowerCase().indexOf(filter.toLowerCase())));
      }

      return stream.collect(Collectors.toList());
   }
}
