package id.seria.gacha.lib.xseries.profiles.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class KeyedLockMap<K> {
   private final Map<K, NulledKeyedLock<K, ?>> locks = new HashMap<>();

   public KeyedLockMap() {
   }

   private NulledKeyedLock<K, ?> createLock(K key) {
      return new NulledKeyedLock<>(this, key);
   }

   public <V> KeyedLock<K, V> lock(K key, Supplier<V> fetcher) {
      Objects.requireNonNull(fetcher, "Value fetcher is null");
      return this.lock(key, k -> fetcher.get());
   }

   public <V> KeyedLock<K, V> lock(K key, Function<K, V> fetcher) {
      Objects.requireNonNull(fetcher, "Value fetcher is null");
      Objects.requireNonNull(key, "Key is null");
      V value = fetcher.apply(key);
      if (value != null) {
         return new FinalizedKeyedLock<>(value);
      }

      NulledKeyedLock<K, V> lock;
      synchronized (this.locks) {
         lock = (NulledKeyedLock<K, V>)this.locks.computeIfAbsent(key, this::createLock);
         boolean isNew = lock.pendingTasks == 0;
         lock.pendingTasks++;
         if (isNew) {
            if (lock.tryLock()) {
               return lock;
            }

            throw new IllegalStateException("Expected first lock holder to be free: " + lock);
         }
      }

      lock.lock();
      return new ReferentialKeyedLock<>(lock, fetcher);
   }

   void unlock(NulledKeyedLock<K, ?> lock) {
      synchronized (this.locks) {
         lock.pendingTasks--;
         if (lock.pendingTasks <= 0) {
            this.locks.remove(lock.key);
         }
      }

      lock.lock.unlock();
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + '(' + this.locks + ')';
   }
}
