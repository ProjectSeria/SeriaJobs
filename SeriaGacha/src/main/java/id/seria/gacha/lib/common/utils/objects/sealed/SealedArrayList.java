package id.seria.gacha.lib.common.utils.objects.sealed;

import java.util.ArrayList;
import java.util.Collection;
import lombok.Generated;
import org.apache.commons.lang.Validate;

public class SealedArrayList<V> extends ArrayList<V> {
   private static final long serialVersionUID = 582113598043164068L;
   protected boolean sealed = true;

   public SealedArrayList(Collection<V> list) {
      if (list != null) {
         this.sealed = false;

         for (V value : list) {
            this.add(value);
         }

         this.sealed = true;
      }
   }

   @Override
   public boolean add(V value) {
      Validate.isTrue(!this.sealed, "ArrayList sealed");
      return super.add(value);
   }

   @Override
   public boolean addAll(Collection<? extends V> l) {
      Validate.isTrue(!this.sealed, "ArrayList sealed");
      return super.addAll(l);
   }

   @Override
   public boolean remove(Object value) {
      Validate.isTrue(!this.sealed, "ArrayList sealed");
      return super.remove(value);
   }

   @Override
   public boolean removeAll(Collection<?> l) {
      Validate.isTrue(!this.sealed, "ArrayList sealed");
      return super.removeAll(l);
   }

   @Override
   public V set(int index, V value) {
      Validate.isTrue(!this.sealed, "ArrayList sealed");
      return super.set(index, value);
   }

   public SealedArrayList<V> modifySafe(Runnable runnable) {
      this.sealed = false;
      runnable.run();
      this.sealed = true;
      return this;
   }

   @Generated
   public boolean isSealed() {
      return this.sealed;
   }

   @Generated
   public SealedArrayList() {
   }
}
