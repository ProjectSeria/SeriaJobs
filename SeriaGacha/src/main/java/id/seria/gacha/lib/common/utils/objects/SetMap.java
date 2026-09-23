package id.seria.gacha.lib.common.utils.objects;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class SetMap<K, V> implements Iterable<Entry<K, Set<V>>> {
   private HashMap<K, Set<V>> hashMap = new HashMap<>();

   public SetMap() {
   }

   public Set<V> get(K key) {
      Set<V> data = this.hashMap.get(key);
      if (data == null) {
         data = this.createSet();
         this.hashMap.put(key, data);
      }

      return data;
   }

   public V add(K key, V value) {
      Set<V> data = this.hashMap.get(key);
      if (data == null) {
         data = this.createSet();
         this.hashMap.put(key, data);
      }

      data.add(value);
      return value;
   }

   public V remove(K key, V value) {
      Set<V> data = this.hashMap.get(key);
      if (data != null) {
         data.remove(value);
      }

      return value;
   }

   public void removeIf(K key, Predicate<? super V> filter) {
      Set<V> data = this.hashMap.get(key);
      if (data != null) {
         data.removeIf(filter);
      }
   }

   public boolean contains(K key, V value) {
      Set<V> data = this.hashMap.get(key);
      return data != null ? data.contains(value) : false;
   }

   public void clear(K key) {
      Set<V> data = this.hashMap.get(key);
      if (data != null) {
         data.clear();
      }
   }

   public int size(K key) {
      Set<V> data = this.hashMap.get(key);
      return data != null ? data.size() : 0;
   }

   public void clear() {
      this.hashMap.clear();
   }

   public Set<Entry<K, Set<V>>> entrySet() {
      return this.hashMap.entrySet();
   }

   public Collection<Set<V>> values() {
      return this.hashMap.values();
   }

   @Override
   public Iterator<Entry<K, Set<V>>> iterator() {
      return this.entrySet().iterator();
   }

   protected Set<V> createSet() {
      return Sets.newHashSet();
   }
}
