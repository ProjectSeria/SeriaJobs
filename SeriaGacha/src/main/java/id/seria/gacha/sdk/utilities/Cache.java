package id.seria.gacha.sdk.utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Cache {
   private static final Map<String, Cache.Entry> cache = new ConcurrentHashMap<>();

   public Cache() {
   }

   public static <T> T fetch(String key, long ttlMillis, Supplier<T> supplier) {
      Cache.Entry entry = cache.get(key);
      if (entry != null && !entry.isExpired()) {
         return (T)entry.value;
      }

      T value = supplier.get();
      cache.put(key, new Cache.Entry(value, ttlMillis));
      return value;
   }

   public static <T> T fetch(String key, Supplier<T> supplier) {
      return fetch(key, -1L, supplier);
   }

   public static void invalidate(String key) {
      cache.remove(key);
   }

   public static void clear() {
      cache.clear();
   }

   private static class Entry {
      final Object value;
      final long expiresAt;

      Entry(Object value, long ttlMillis) {
         this.value = value;
         this.expiresAt = ttlMillis > 0L ? System.currentTimeMillis() + ttlMillis : -1L;
      }

      boolean isExpired() {
         return this.expiresAt != -1L && System.currentTimeMillis() > this.expiresAt;
      }
   }
}
