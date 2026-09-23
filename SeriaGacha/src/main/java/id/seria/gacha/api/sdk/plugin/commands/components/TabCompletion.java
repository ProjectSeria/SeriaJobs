package id.seria.gacha.api.sdk.plugin.commands.components;

import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface TabCompletion<T extends InvocationContext> {
   List<String> tabComplete(T var1);

   default TabCompletion<T> and(List<String> join) {
      return context -> {
         List<String> result = new ArrayList<>(this.tabComplete(context));
         result.addAll(join);
         return result;
      };
   }

   default TabCompletion<T> and(String... join) {
      return this.and(Arrays.asList(join));
   }
}
