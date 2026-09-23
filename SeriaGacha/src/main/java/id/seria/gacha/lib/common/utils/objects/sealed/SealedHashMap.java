package id.seria.gacha.lib.common.utils.objects.sealed;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.apache.commons.lang.Validate;

public class SealedHashMap<K, V> extends HashMap<K, V> {
   private static final long serialVersionUID = 582113598043164068L;
   private boolean sealed = true;

   public SealedHashMap(Map<K, V> map) {
      if (map != null) {
         this.sealed = false;

         for (Entry<K, V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
         }

         this.sealed = true;
      }
   }

   @Override
   public V put(K key, V value) {
      Validate.isTrue(!this.sealed, "HashMap sealed");
      return super.put(key, value);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> m) {
      Validate.isTrue(!this.sealed, "HashMap sealed");
      super.putAll(m);
   }

   @Override
   public boolean remove(Object key, Object value) {
      Validate.isTrue(!this.sealed, "HashMap sealed");
      return super.remove(key, value);
   }

   @Override
   public boolean replace(K key, V oldValue, V newValue) {
      Validate.isTrue(!this.sealed, "HashMap sealed");
      return super.replace(key, oldValue, newValue);
   }

   @Override
   public V replace(K key, V value) {
      Validate.isTrue(!this.sealed, "HashMap sealed");
      return super.replace(key, value);
   }

   @Generated
   public boolean isSealed() {
      return this.sealed;
   }

   @Generated
   public void setSealed(boolean sealed) {
      this.sealed = sealed;
   }

   @Generated
   public SealedHashMap() {
   }
}
