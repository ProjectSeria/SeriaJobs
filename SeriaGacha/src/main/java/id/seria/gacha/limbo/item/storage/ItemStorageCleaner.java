package id.seria.gacha.limbo.item.storage;

import id.seria.gacha.api.internal.Item;
import id.seria.gacha.limbo.item.CustomItem;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ItemStorageCleaner {
   private ItemStorageCleaner() {
   }

   public static void removeUnused(ItemStorageScope scope, Collection<? extends Item> referencedItems) {
      if (scope.getInternalStorageItems() != null && !scope.getInternalStorageItems().isEmpty()) {
         Set<String> used = new HashSet<>();

         for (Item item : referencedItems) {
            collectReference(scope.getStorageScopeId(), item, used);
         }

         scope.getInternalStorageItems().keySet().removeIf(id -> !used.contains(id));
      }
   }

   private static void collectReference(String scopeId, Item item, Set<String> used) {
      if (item instanceof CustomItem) {
         String reference = ((CustomItem)item).getReference();
         if (reference != null) {
            int index = reference.indexOf(58);
            if (index > 0 && index < reference.length() - 1) {
               if (reference.substring(0, index).equals(scopeId)) {
                  used.add(reference.substring(index + 1));
               }
            }
         }
      }
   }
}
