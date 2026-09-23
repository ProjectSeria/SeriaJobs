package id.seria.gacha.lib.common.uicomponents.newest.container.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus<T> {
   private final Map<Object, List<Consumer<T>>> subscribers = new HashMap<>();

   public EventBus() {
   }

   public EventBus<T> subscribe(Class<? extends T> eventType, Consumer<T> subscriber) {
      return this.subscribeInternal(eventType, subscriber);
   }

   public EventBus<T> subscribe(String key, Consumer<T> subscriber) {
      return this.subscribeInternal(key, subscriber);
   }

   public void publish(T event) {
      for (Consumer<T> handler : this.subscribers.getOrDefault(event.getClass(), Collections.emptyList())) {
         handler.accept(event);
      }
   }

   public void publish(String key) {
      for (Consumer<T> handler : this.subscribers.getOrDefault(key, Collections.emptyList())) {
         handler.accept(null);
      }
   }

   private EventBus<T> subscribeInternal(Object key, Consumer<T> subscriber) {
      this.subscribers.computeIfAbsent(key, k -> new ArrayList<>()).add(subscriber);
      return this;
   }
}
