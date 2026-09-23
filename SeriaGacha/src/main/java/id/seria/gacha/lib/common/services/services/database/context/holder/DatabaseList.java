package id.seria.gacha.lib.common.services.services.database.context.holder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;

public class DatabaseList<V extends DatabaseModelHolder<?>> extends HashSet<V> {
   private static final long serialVersionUID = -2315199106272678986L;
   private final Map<V, DatabaseList.DatabaseAction> objectsToSync = new HashMap<>();

   public DatabaseList(Collection<V> collection) {
      super(collection);
   }

   public boolean add(V value) {
      synchronized (this.objectsToSync) {
         this.objectsToSync.put(value, DatabaseList.DatabaseAction.INSERT);
      }

      return super.add(value);
   }

   @Override
   public boolean addAll(Collection<? extends V> l) {
      boolean modified = false;

      for (V value : l) {
         if (value.getDatabaseId() == -1) {
            synchronized (this.objectsToSync) {
               this.objectsToSync.put(value, DatabaseList.DatabaseAction.INSERT);
            }
         }

         modified |= super.add(value);
      }

      return modified;
   }

   public boolean remove(V value) {
      if (super.remove(value)) {
         if (value.getDatabaseId() > -1) {
            synchronized (this.objectsToSync) {
               this.objectsToSync.put(value, DatabaseList.DatabaseAction.DELETE);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean removeAll(Collection<?> l) {
      boolean modified = false;

      for (Object value : l) {
         if (super.remove(value)) {
            if (((DatabaseModelHolder)value).getDatabaseId() > -1) {
               synchronized (this.objectsToSync) {
                  this.objectsToSync.put((V)value, DatabaseList.DatabaseAction.DELETE);
               }
            }

            modified = true;
         }
      }

      return modified;
   }

   public Map<V, DatabaseList.DatabaseAction> getDirtyObjects() {
      synchronized (this.objectsToSync) {
         if (this.objectsToSync.size() > 0) {
            Iterator<Entry<V, DatabaseList.DatabaseAction>> iterator = this.objectsToSync.entrySet().iterator();

            while (iterator.hasNext()) {
               Entry<V, DatabaseList.DatabaseAction> entry = iterator.next();
               V value = entry.getKey();
               DatabaseList.DatabaseAction action = entry.getValue();
               if (action == DatabaseList.DatabaseAction.INSERT && (!super.contains(value) || value.getDatabaseId() != -1)
                  || action == DatabaseList.DatabaseAction.DELETE && (super.contains(value) || value.getDatabaseId() == -1)) {
                  iterator.remove();
               }
            }
         }

         for (V value : this) {
            if (value.isDirty() && value.getDatabaseId() != -1) {
               this.objectsToSync.putIfAbsent(value, DatabaseList.DatabaseAction.UPDATE);
            }
         }

         return this.objectsToSync;
      }
   }

   public void clearDirtyObjects() {
      synchronized (this.objectsToSync) {
         for (V value : this.objectsToSync.keySet()) {
            value.unmarkDirty();
         }

         this.objectsToSync.clear();
      }
   }

   @Generated
   public DatabaseList() {
   }

   public enum DatabaseAction {
      INSERT,
      UPDATE,
      DELETE;

      DatabaseAction() {
      }
   }
}
