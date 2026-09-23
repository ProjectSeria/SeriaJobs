package id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list;

import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import lombok.Generated;
import lombok.NonNull;

public class SimplePaginatedList<T> extends PaginatedList<T> {
   private Function<ContainerView, List<T>> itemsSupplier;
   private List<T> cachedItems;
   private int cachedItemsSize;

   public SimplePaginatedList(SlotCompound slots, @NonNull PagingData paging, Function<ContainerView, List<T>> itemsSupplier) {
      super(slots, paging);
      if (paging == null) {
         throw new NullPointerException("paging is marked non-null but is null");
      }

      this.itemsSupplier = itemsSupplier;
   }

   public SimplePaginatedList(SlotCompound slots, @NonNull PagingData paging, List<T> items) {
      super(slots, paging);
      if (paging == null) {
         throw new NullPointerException("paging is marked non-null but is null");
      }

      this.itemsSupplier = view -> Collections.synchronizedList(items);
   }

   @Override
   public List<T> onPrepareItems(RenderViewContext context, int startIndex, int endIndex) {
      this.cachedItems = new ArrayList<>(this.itemsSupplier.apply(context.getContainerView()));
      this.cachedItemsSize = this.cachedItems.size();
      int fromIndex = Math.max(0, startIndex);
      int toIndex = Math.min(this.cachedItems.size(), endIndex);
      if (fromIndex > toIndex) {
         fromIndex = toIndex;
      }

      return this.cachedItems.subList(fromIndex, toIndex);
   }

   @Override
   public int countMaxItems() {
      return this.cachedItemsSize;
   }

   @Generated
   public Function<ContainerView, List<T>> getItemsSupplier() {
      return this.itemsSupplier;
   }

   @Generated
   public List<T> getCachedItems() {
      return this.cachedItems;
   }

   @Generated
   public int getCachedItemsSize() {
      return this.cachedItemsSize;
   }

   @Generated
   public SimplePaginatedList<T> setItemsSupplier(Function<ContainerView, List<T>> itemsSupplier) {
      this.itemsSupplier = itemsSupplier;
      return this;
   }
}
