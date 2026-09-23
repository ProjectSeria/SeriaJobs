package id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list;

import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.ClickableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.Generated;
import lombok.NonNull;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class PaginatedList<T> extends ClickableComponent<PaginatedList<T>> {
   public static String CURRENT_PAGE_KEY = "components.%s.current_page";
   public static String MAX_PAGES_KEY = "components.%s.max_pages";
   public static String ITEMS_KEY = "components.%s.items";
   protected final PagingData paging;
   private Function<RenderViewContext, VirtualItem> addItemSlot = this::onPrepareAddItem;
   protected Function<RenderViewContext, VirtualItem> fallbackItem = VirtualItem::air;
   private PaginatedList.ItemRenderConsumer<T> onItemRender = (context, index, display, item) -> {};
   private PaginatedList.ItemClickConsumer<T> onItemClick = (context, index, item) -> {};

   public PaginatedList(SlotCompound slots, @NonNull PagingData paging) {
      super(slots, new ComponentData());
      if (paging == null) {
         throw new NullPointerException("paging is marked non-null but is null");
      }

      this.paging = paging;
   }

   public abstract List<T> onPrepareItems(RenderViewContext var1, int var2, int var3);

   public abstract int countMaxItems();

   public List<T> getPreparedItems() {
      return (List<T>)this.getContainerView().getMetadata(String.format(ITEMS_KEY, this.id));
   }

   @Deprecated
   public VirtualItem onPrepareAddItem(RenderViewContext context) {
      return VirtualItem.air();
   }

   @Deprecated
   public void onRenderItem(RenderViewContext context, int index, VirtualItem virtualItem, T item) {
      this.onItemRender.accept(context, index, virtualItem, item);
      this.onItemRender(context, index, virtualItem, item);
   }

   @Deprecated
   public void onItemRender(RenderViewContext context, int index, VirtualItem virtualItem, T item) {
   }

   @Deprecated
   public void onItemClick(ClickViewContext context, int index, T item) {
      this.onItemClick.accept(context, index, item);
   }

   @Override
   public void onPreRender(RenderViewContext context) {
      ContainerView view = context.getContainerView();
      int maxItemsPerPage = this.getSlot().length();
      int startIndex = (this.getCurrentPage() - 1) * maxItemsPerPage;
      List<T> items = this.onPrepareItems(context, startIndex, startIndex + maxItemsPerPage);
      view.setMetadata(String.format(ITEMS_KEY, this.id), items, true);
      int maxPages = items.isEmpty() ? 1 : (int)Math.ceil((double)(this.countMaxItems() + (this.containsAddSlot(context) ? 1 : 0)) / maxItemsPerPage);
      view.setMetadata(String.format(MAX_PAGES_KEY, this.id), maxPages, true);
   }

   @Override
   public void onRender(RenderViewContext context) {
      int maxItemsPerPage = this.getSlot().length();
      List<T> items = (List<T>)this.getContainerView().getMetadata(String.format(ITEMS_KEY, this.id));
      int maxPages = items.isEmpty() ? 1 : (int)Math.ceil((double)(this.countMaxItems() + (this.containsAddSlot(context) ? 1 : 0)) / maxItemsPerPage);
      if (this.paging != null) {
         if (this.getCurrentPage() != 1) {
            int previousPage = this.getCurrentPage() - 1;
            new VirtualItem()
               .withSlots(this.paging.getPreviousPageController().getSlotsCompound())
               .withItem(this.paging.getPreviousPageController().getMaterial())
               .withCustomModelData(this.paging.getPreviousPageController().getCustomModelData())
               .withItemModel(this.paging.getPreviousPageController().getItemModel())
               .withDisplayName(this._r(this._t(this.paging.getPreviousPageController().getName())[0], "%page%", previousPage))
               .withLore(this._r(Arrays.asList(this._t(this.paging.getPreviousPageController().getLore())), "%page%", previousPage))
               .whenClick(click -> {
                  click.getContainerView().getEventBus().publish("previous-page");
                  this.setCurrentPage(this.getCurrentPage() - 1);
                  this.getContainerView().updateView();
               })
               .render(context);
         }

         if (this.getCurrentPage() != maxPages && maxPages != 1) {
            int nextPage = this.getCurrentPage() + 1;
            new VirtualItem()
               .withSlots(this.paging.getNextPageController().getSlotsCompound())
               .withItem(this.paging.getNextPageController().getMaterial())
               .withCustomModelData(this.paging.getNextPageController().getCustomModelData())
               .withItemModel(this.paging.getNextPageController().getItemModel())
               .withDisplayName(this._r(this._t(this.paging.getNextPageController().getName())[0], "%page%", nextPage))
               .withLore(this._r(Arrays.asList(this._t(this.paging.getNextPageController().getLore())), "%page%", nextPage))
               .whenClick(click -> {
                  click.getContainerView().getEventBus().publish("next-page");
                  this.setCurrentPage(this.getCurrentPage() + 1);
                  this.getContainerView().updateView();
               })
               .render(context);
         }
      }

      int i = 0;
      int startIndex = (this.getCurrentPage() - 1) * maxItemsPerPage;
      boolean addButtonAdded = false;
      PaginationSettings.ItemsDirection direction = this.getItemsDirection();
      List<int[]> groups = direction == PaginationSettings.ItemsDirection.HORIZONTAL ? this.getSlot().horizontalGroups() : this.getSlot().verticalGroups();
      boolean addSlotPresent = this.containsAddSlot(context);

      for (int[] group : groups) {
         int usable = 0;

         for (int slot : group) {
            int[] position = InventoryUtil.getXYPosition(slot);
            if (position[0] > 9 || position[1] > context.getContainerType().getRows()) {
               break;
            }

            Objects.requireNonNull(this.fallbackItem.apply(context)).withSlot(slot).render(context);
            usable++;
         }

         if (usable != 0) {
            int remaining = Math.max(0, items.size() - i);
            int occupied = MathUtil.clamp(remaining + (addSlotPresent && !addButtonAdded ? 1 : 0), 0, usable);
            if (occupied != 0) {
               int offset;
               switch (this.getItemsAlign()) {
                  case RIGHT:
                     offset = usable - occupied;
                     break;
                  case CENTER:
                     offset = (usable - occupied) / 2;
                     break;
                  case LEFT:
                  default:
                     offset = 0;
               }

               for (int j = offset; j < offset + occupied; j++) {
                  int[] position = InventoryUtil.getXYPosition(group[j]);
                  if (i >= items.size()) {
                     if (!addButtonAdded) {
                        Objects.requireNonNull(this.addItemSlot.apply(context)).withSlot(position[0], position[1]).render(context);
                        addButtonAdded = true;
                     }
                     break;
                  }

                  T item = items.get(i);
                  VirtualItem virtualItem = new VirtualItem().withSlot(position[0], position[1]);
                  this.onRenderItem(context, startIndex + i, virtualItem, item);
                  virtualItem.withNbtTag("page-index", i).withNbtTag("item-index", startIndex + i).render(context);
                  i++;
               }
            }
         }
      }
   }

   private PaginationSettings.ItemsAlign getItemsAlign() {
      return this.paging == null ? PaginationSettings.ItemsAlign.LEFT : this.paging.getItemsAlign();
   }

   private PaginationSettings.ItemsDirection getItemsDirection() {
      return this.paging == null ? PaginationSettings.ItemsDirection.HORIZONTAL : this.paging.getItemsDirection();
   }

   protected boolean containsAddSlot(RenderViewContext context) {
      if (this.addItemSlot == null) {
         return false;
      }

      VirtualItem test = this.addItemSlot.apply(context);
      return test != null && !test.getItem().getType().equals(Material.AIR);
   }

   @Override
   public void onClick(ClickViewContext context) {
      ItemStack itemStack = context.getCurrentItem();
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         ItemBuilder wrapper = ItemBuilder.of(itemStack);
         List<T> items = (List<T>)context.getContainerView().getMetadata(String.format(ITEMS_KEY, this.id));
         int pageIndex = wrapper.getNbtTagInt("page-index");
         if (pageIndex >= 0 && pageIndex < items.size()) {
            T item = items.get(pageIndex);
            int index = wrapper.getNbtTagInt("item-index");
            this.onItemClick(context, index, item);
         }
      }
   }

   public PaginatedList<T> setAddItemSlot(Function<RenderViewContext, VirtualItem> addItemSlot) {
      if (addItemSlot == null) {
         this.addItemSlot = ctx -> VirtualItem.air();
      } else {
         this.addItemSlot = addItemSlot;
      }

      return this;
   }

   public PaginatedList<T> setFallbackItem(Function<RenderViewContext, VirtualItem> fallbackItem) {
      this.fallbackItem = fallbackItem;
      return this;
   }

   @Deprecated
   public PaginatedList<T> setAddItemSlot(VirtualItem item) {
      if (item == null) {
         item = VirtualItem.air();
      }

      VirtualItem finalItem = item;
      this.addItemSlot = ctx -> finalItem;
      return this;
   }

   @Deprecated
   public PaginatedList<T> setFallbackItem(VirtualItem item) {
      this.fallbackItem = ctx -> item;
      return this;
   }

   @Override
   public List<ClickAction> getDisplayActions() {
      throw new NotImplementedException("PaginatedList don't support getDisplayActions");
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext renderViewContext) {
      throw new NotImplementedException("PaginatedList don't support getRenderDisplay");
   }

   public int getCurrentPage() {
      return this.getContainerView().getMetadata(String.format(CURRENT_PAGE_KEY, this.id), 1);
   }

   public int setCurrentPage(int currentPage) {
      this.getContainerView().setMetadata(String.format(CURRENT_PAGE_KEY, this.id), currentPage, true);
      return currentPage;
   }

   public int getMaxPages() {
      return this.getContainerView().getMetadata(String.format(MAX_PAGES_KEY, this.id), 1);
   }

   public final int getPreviousPage() {
      return Math.max(1, this.getCurrentPage() - 1);
   }

   public final int getNextPage() {
      return Math.min(this.getMaxPages(), this.getCurrentPage() + 1);
   }

   public final boolean isFirstPage() {
      return this.getCurrentPage() == 1;
   }

   public final boolean isLastPage() {
      return this.getCurrentPage() == this.getMaxPages();
   }

   public final void switchTo(int page) {
      this.setCurrentPage(MathUtil.clamp(page, 1, this.getMaxPages()));
      this.getContainerView().updateView();
   }

   public final boolean switchToPreviousPage() {
      if (this.isFirstPage()) {
         return false;
      }

      this.switchTo(this.getCurrentPage() - 1);
      return true;
   }

   public final boolean switchToNextPage() {
      if (this.isLastPage()) {
         return false;
      }

      this.switchTo(this.getCurrentPage() + 1);
      return true;
   }

   @Generated
   public PagingData getPaging() {
      return this.paging;
   }

   @Generated
   public Function<RenderViewContext, VirtualItem> getAddItemSlot() {
      return this.addItemSlot;
   }

   @Generated
   public Function<RenderViewContext, VirtualItem> getFallbackItem() {
      return this.fallbackItem;
   }

   @Generated
   public PaginatedList.ItemRenderConsumer<T> getOnItemRender() {
      return this.onItemRender;
   }

   @Generated
   public PaginatedList.ItemClickConsumer<T> getOnItemClick() {
      return this.onItemClick;
   }

   @Generated
   public PaginatedList<T> setOnItemRender(PaginatedList.ItemRenderConsumer<T> onItemRender) {
      this.onItemRender = onItemRender;
      return this;
   }

   @Generated
   public PaginatedList<T> setOnItemClick(PaginatedList.ItemClickConsumer<T> onItemClick) {
      this.onItemClick = onItemClick;
      return this;
   }

   @FunctionalInterface
   public interface ItemClickConsumer<T> {
      void accept(ClickViewContext var1, int var2, T var3);
   }

   @FunctionalInterface
   public interface ItemRenderConsumer<T> {
      void accept(RenderViewContext var1, int var2, VirtualItem var3, T var4);
   }
}
