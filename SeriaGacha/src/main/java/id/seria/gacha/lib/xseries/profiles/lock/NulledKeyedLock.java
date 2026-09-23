package id.seria.gacha.lib.xseries.profiles.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
final class NulledKeyedLock<K, V> implements KeyedLock<K, V> {
   protected final Lock lock = new ReentrantLock();
   private final KeyedLockMap<K> map;
   protected final K key;
   protected int pendingTasks;

   protected NulledKeyedLock(KeyedLockMap<K> map, K key) {
      this.map = map;
      this.key = key;
   }

   @Override
   public V getOrRetryValue() {
      return null;
   }

   @Override
   public void lock() {
      this.lock.lock();
   }

   protected boolean tryLock() {
      return this.lock.tryLock();
   }

   @Override
   public void unlock() {
      this.map.unlock(this);
   }

   @Override
   public void close() {
      this.unlock();
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "(key=" + this.key + ", pendingTasks=" + this.pendingTasks + ')';
   }
}
