package id.seria.gacha.api.sdk.locales.translatable;

import java.util.function.Function;

public interface Translation {
   boolean isEmpty();

   Translation postProcessor(Function<String, String> var1);

   Translation arguments(Object... var1);

   Translation colorless();

   Translation lowercase();

   String asString();
}
