package id.seria.gacha.lib.xseries.profiles.lock;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
final class FinalizedKeyedLock<K, V> implements KeyedLock<K, V> {
   private final V value;

   protected FinalizedKeyedLock(V value) {
      this.value = value;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + '(' + this.value + ')';
   }

   @Override
   public V getOrRetryValue() {
      return this.value;
   }

   @Override
   public void lock() {
   }

   @Override
   public void unlock() {
   }

   @Override
   public void close() {
   }
}
