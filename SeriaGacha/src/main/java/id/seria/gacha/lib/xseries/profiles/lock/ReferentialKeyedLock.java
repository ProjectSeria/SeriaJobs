package id.seria.gacha.lib.xseries.profiles.lock;

import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
final class ReferentialKeyedLock<K, V> implements KeyedLock<K, V> {
   protected final Function<K, V> fetcher;
   protected final NulledKeyedLock<K, V> lock;
   private V value;

   protected ReferentialKeyedLock(NulledKeyedLock<K, V> lock, Function<K, V> fetcher) {
      this.lock = lock;
      this.fetcher = fetcher;
   }

   @Override
   public V getOrRetryValue() {
      if (this.value == null) {
         this.value = this.fetcher.apply(this.lock.key);
      }

      return this.value;
   }

   @Override
   public void lock() {
      this.lock.lock();
   }

   @Override
   public void unlock() {
      this.lock.unlock();
   }

   @Override
   public void close() {
      this.unlock();
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName()
         + "(lock="
         + this.lock
         + ", value="
         + this.value
         + ", fetcher="
         + (this.fetcher == null ? "null" : this.fetcher.getClass().getSimpleName())
         + ')';
   }
}
