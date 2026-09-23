package id.seria.gacha.lib.common.utils.objects;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class ListMap<K, V> implements Iterable<Entry<K, List<V>>> {
   private HashMap<K, List<V>> hashMap = new HashMap<>();

   public ListMap() {
   }

   public HashMap<K, List<V>> map() {
      return this.hashMap;
   }

   public List<V> get(K key) {
      List<V> data = this.hashMap.get(key);
      if (data == null) {
         data = this.createList();
         this.hashMap.put(key, data);
      }

      return data;
   }

   public V add(K key, V value) {
      List<V> data = this.hashMap.get(key);
      if (data == null) {
         data = this.createList();
         this.hashMap.put(key, data);
      }

      data.add(value);
      return value;
   }

   public V add(K key, int index, V value) {
      List<V> data = this.hashMap.get(key);
      if (data == null) {
         data = this.createList();
         this.hashMap.put(key, data);
      }

      data.add(index, value);
      return value;
   }

   public V remove(K key, V value) {
      List<V> data = this.hashMap.get(key);
      if (data != null) {
         data.remove(value);
         if (data.isEmpty()) {
            this.hashMap.remove(key);
         }
      }

      return value;
   }

   public List<V> remove(K key) {
      return this.hashMap.remove(key);
   }

   public void removeIf(K key, Predicate<? super V> filter) {
      List<V> data = this.hashMap.get(key);
      if (data != null) {
         data.removeIf(filter);
         if (data.isEmpty()) {
            this.hashMap.remove(key);
         }
      }
   }

   public boolean contains(K key, V value) {
      List<V> data = this.hashMap.get(key);
      return data != null ? data.contains(value) : false;
   }

   public void clear(K key) {
      List<V> data = this.hashMap.get(key);
      if (data != null) {
         data.clear();
         this.hashMap.remove(key);
      }
   }

   public int size(K key) {
      List<V> data = this.hashMap.get(key);
      return data != null ? data.size() : 0;
   }

   public boolean isEmpty() {
      return this.hashMap.isEmpty();
   }

   public void clear() {
      this.hashMap.clear();
   }

   public Set<Entry<K, List<V>>> entrySet() {
      return this.hashMap.entrySet();
   }

   public Collection<List<V>> values() {
      return this.hashMap.values();
   }

   @Override
   public Iterator<Entry<K, List<V>>> iterator() {
      return this.entrySet().iterator();
   }

   protected List<V> createList() {
      return Lists.newArrayList();
   }
}
