package id.seria.gacha.api.sdk.plugin.eventbus;

import java.util.function.Consumer;

public interface EventBus {
   <T> void subscribe(Class<T> var1, Consumer<T> var2);

   <T> void trigger(T var1);
}
