package id.seria.gacha.sdk.utilities.objects;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ExpiringHashMap<K, V> {
   private final long expirationMillis;
   private final ConcurrentHashMap<K, ExpiringHashMap<K, V>.ExpiringValue<V>> map = new ConcurrentHashMap<>();

   public ExpiringHashMap(long expirationMillis) {
      this.expirationMillis = expirationMillis;
   }

   public boolean containsKey(K key) {
      ExpiringHashMap<K, V>.ExpiringValue<V> value = this.map.get(key);
      if (value != null && !value.isExpired()) {
         return true;
      }

      this.map.remove(key);
      return false;
   }

   public V get(K key) {
      ExpiringHashMap<K, V>.ExpiringValue<V> value = this.map.get(key);
      if (value != null && !value.isExpired()) {
         return value.getValue();
      }

      this.map.remove(key);
      return null;
   }

   public void put(K key, V value) {
      this.map.put(key, new ExpiringValue<>(value, System.currentTimeMillis()));
   }

   public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
      ExpiringValue<V> current = this.map.get(key);
      if (current != null && !current.isExpired()) {
         return current.getValue();
      }

      V newValue = mappingFunction.apply(key);
      this.map.put(key, new ExpiringValue<>(newValue, System.currentTimeMillis()));
      return newValue;
   }

   public V remove(K key) {
      ExpiringHashMap<K, V>.ExpiringValue<V> value = this.map.remove(key);
      return value != null && !value.isExpired() ? value.getValue() : null;
   }

   private class ExpiringValue<T> {
      private final T value;
      private final long createdAt;

      ExpiringValue(T value, long createdAt) {
         this.value = value;
         this.createdAt = createdAt;
      }

      T getValue() {
         return this.value;
      }

      boolean isExpired() {
         return System.currentTimeMillis() - this.createdAt > ExpiringHashMap.this.expirationMillis;
      }
   }
}
