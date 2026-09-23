package id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list;

import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.ClickableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.Generated;
import lombok.NonNull;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Deprecated
public abstract class AsyncPaginatedList<T> extends ClickableComponent<AsyncPaginatedList<T>> {
   public static String CURRENT_PAGE_KEY = "components.%s.current_page";
   public static String MAX_PAGES_KEY = "components.%s.max_pages";
   public static String ITEMS_KEY = "components.%s.items";
   public static String MAX_ITEMS_KEY = "components.%s.max_items";
   public static String COMPLETABLE_FUTURE_KEY = "components.%s.completable_future";
   protected final PagingData paging;
   protected VirtualItem.VirtualItemConsumer fallbackItem = (ctx, view) -> view.withItem(XMaterial.AIR);

   public AsyncPaginatedList(SlotCompound slots, @NonNull PagingData paging) {
      super(slots, new ComponentData());
      if (paging == null) {
         throw new NullPointerException("paging is marked non-null but is null");
      }

      this.paging = paging;
   }

   public abstract CompletableFuture<List<T>> onPrepareItems(ContainerView var1, int var2, int var3);

   public abstract CompletableFuture<Integer> countMaxItems(ContainerView var1);

   public abstract void onRenderItem(RenderViewContext var1, int var2, VirtualItem var3, T var4);

   public abstract void onItemClick(ClickViewContext var1, int var2, T var3);

   public abstract VirtualItem prepareLoadingSlot();

   @Override
   public void onPreRender(RenderViewContext context) {
      ContainerView view = context.getContainerView();
      CompletableFuture<Void> currentTask = (CompletableFuture<Void>)view.getMetadata(String.format(COMPLETABLE_FUTURE_KEY, this.id));
      if (currentTask != null && !currentTask.isDone()) {
         try {
            currentTask.cancel(true);
         } catch (CancellationException var9) {
         }
      }

      int maxItemsPerPage = this.getSlot().length();
      int startIndex = (this.getCurrentPage(view) - 1) * maxItemsPerPage;
      CompletableFuture<List<T>> futureItems = this.onPrepareItems(view, startIndex, startIndex + maxItemsPerPage);
      CompletableFuture<Integer> futureMaxItems = this.countMaxItems(view);
      CompletableFuture<Void> combinedFuture = futureItems.thenCombine(futureMaxItems, (items, maxItems) -> {
         view.setMetadata(String.format(ITEMS_KEY, this.id), items, true);
         view.setMetadata(String.format(MAX_ITEMS_KEY, this.id), maxItems, true);
         return null;
      });
      combinedFuture.<Boolean>handle((unused, ex) -> {
         if (ex != null) {
            if (!this.isExpectedCancellation(ex)) {
               ex.printStackTrace();
            }

            return false;
         } else {
            return true;
         }
      }).thenAccept(success -> {
         if (success) {
            context.getPlugin().getScheduler().runSync(() -> {
               RenderViewContext other = new RenderViewContext(context.getContainer(), context.getContainerView());
               this.onRenderItems(other);
               SlotCompound slots = this.getSlot().clone();
               if (this.getCurrentPage(view) != 1) {
                  slots.with(this.paging.getPreviousPageController().getSlotsCompound());
               }

               int maxItems = (Integer)context.getContainerView().getMetadata(String.format(MAX_ITEMS_KEY, this.id));
               List<T> items = (List<T>)context.getContainerView().getMetadata(String.format(ITEMS_KEY, this.id));
               int maxPages = items.isEmpty() ? 1 : (int)Math.ceil((double)maxItems / maxItemsPerPage);
               if (this.getCurrentPage(view) != maxPages && maxPages != 1) {
                  slots.with(this.paging.getNextPageController().getSlotsCompound());
               }

               for (int slot : slots.asArray()) {
                  if (other.getSlots()[slot] == null) {
                     view.getInventory().setItem(slot, new ItemStack(Material.AIR));
                  } else {
                     VirtualItem item = new VirtualItem().withSlot(slot).withItem(other.getSlots()[slot]);
                     other.getContainer().onRenderSlot(other, item);
                     item.render(view.getInventory());
                     view.getConsumers()[slot] = other.getConsumers()[slot];
                  }
               }

               view.getViewer().updateInventory();
            });
         }
      });
      view.setMetadata(String.format(COMPLETABLE_FUTURE_KEY, this.id), combinedFuture, true);
   }

   private boolean isExpectedCancellation(Throwable throwable) {
      if (throwable == null) {
         return false;
      } else if (throwable instanceof CancellationException) {
         return true;
      } else {
         return throwable instanceof CompletionException
            ? this.isExpectedCancellation(throwable.getCause())
            : this.isExpectedCancellation(throwable.getCause());
      }
   }

   @Override
   public void onRender(RenderViewContext context) {
      VirtualItem item = this.prepareLoadingSlot();
      item.withSlots(this.getSlot()).render(context);
   }

   public void onRenderItems(RenderViewContext context) {
      ContainerView view = context.getContainerView();
      int maxItemsPerPage = this.getSlot().length();
      List<T> items = (List<T>)context.getContainerView().getMetadata(String.format(ITEMS_KEY, this.id));
      int maxItems = (Integer)context.getContainerView().getMetadata(String.format(MAX_ITEMS_KEY, this.id));
      int maxPages = items.isEmpty() ? 1 : (int)Math.ceil((double)maxItems / maxItemsPerPage);
      if (this.getCurrentPage(view) != 1) {
         int previousPage = this.getCurrentPage(view) - 1;
         new VirtualItem()
            .withSlots(this.paging.getPreviousPageController().getSlotsCompound())
            .withItem(this.paging.getPreviousPageController().getMaterial())
            .withCustomModelData(this.paging.getPreviousPageController().getCustomModelData())
            .withItemModel(this.paging.getPreviousPageController().getItemModel())
            .withDisplayName(this._r(this._t(this.paging.getPreviousPageController().getName())[0], "%page%", previousPage))
            .withLore(this._r(Arrays.asList(this._t(this.paging.getPreviousPageController().getLore())), "%page%", previousPage))
            .whenClick(click -> {
               click.getContainerView().getEventBus().publish("previous-page");
               this.setCurrentPage(view, this.getCurrentPage(view) - 1);
               view.updateView();
            })
            .render(context);
      }

      if (this.getCurrentPage(view) != maxPages && maxPages != 1) {
         int nextPage = this.getCurrentPage(view) + 1;
         new VirtualItem()
            .withSlots(this.paging.getNextPageController().getSlotsCompound())
            .withItem(this.paging.getNextPageController().getMaterial())
            .withCustomModelData(this.paging.getNextPageController().getCustomModelData())
            .withItemModel(this.paging.getNextPageController().getItemModel())
            .withDisplayName(this._r(this._t(this.paging.getNextPageController().getName())[0], "%page%", nextPage))
            .withLore(this._r(Arrays.asList(this._t(this.paging.getNextPageController().getLore())), "%page%", nextPage))
            .whenClick(click -> {
               click.getContainerView().getEventBus().publish("next-page");
               this.setCurrentPage(view, this.getCurrentPage(view) + 1);
               view.updateView();
            })
            .render(context);
      }

      int i = 0;
      int startIndex = (this.getCurrentPage(view) - 1) * maxItemsPerPage;

      for (int[] group : this.getSlot().horizontalGroups()) {
         int usable = 0;

         for (int slot : group) {
            int[] position = InventoryUtil.getXYPosition(slot);
            if (position[0] > 9 || position[1] > context.getContainerType().getRows()) {
               break;
            }

            this.fallbackItem.setItem(context, position[0], position[1]);
            usable++;
         }

         if (usable != 0) {
            int occupied = MathUtil.clamp(items.size() - i, 0, usable);
            if (occupied != 0) {
               int offset;
               switch (this.paging.getItemsAlign()) {
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
                  T item = items.get(i);
                  VirtualItem virtualItem = new VirtualItem();
                  this.onRenderItem(context, startIndex + i, virtualItem, item);
                  virtualItem.withSlot(position[0], position[1]).withNbtTag("item-index", i).render(context);
                  i++;
               }
            }
         }
      }
   }

   @Override
   public void onClick(ClickViewContext context) {
      ItemStack itemStack = context.getCurrentItem();
      if (itemStack != null && itemStack.getType() != Material.AIR) {
         ItemBuilder wrapper = ItemBuilder.of(itemStack);
         List<T> items = (List<T>)context.getContainerView().getMetadata(String.format(ITEMS_KEY, this.id));
         int index = wrapper.getNbtTagInt("item-index");
         if (index >= 0 && index < items.size()) {
            T item = items.get(index);
            if (item != null) {
               this.onItemClick(context, index, item);
            }
         }
      }
   }

   public int getCurrentPage(ContainerView view) {
      return view.getMetadata(String.format(CURRENT_PAGE_KEY, this.id), 1);
   }

   public int setCurrentPage(ContainerView view, int currentPage) {
      view.setMetadata(String.format(CURRENT_PAGE_KEY, this.id), currentPage, true);
      return currentPage;
   }

   public int getMaxPages(ContainerView view) {
      return view.getMetadata(String.format(MAX_PAGES_KEY, this.id), 1);
   }

   public final int getPreviousPage(ContainerView view) {
      return Math.max(1, this.getCurrentPage(view) - 1);
   }

   public final int getNextPage(ContainerView view) {
      return Math.min(this.getMaxPages(view), this.getCurrentPage(view) + 1);
   }

   public final boolean isFirstPage(ContainerView view) {
      return this.getCurrentPage(view) == 1;
   }

   public final boolean isLastPage(ContainerView view) {
      return this.getCurrentPage(view) == this.getMaxPages(view);
   }

   public final void switchTo(ContainerView view, int page) {
      this.setCurrentPage(view, MathUtil.clamp(page, 1, this.getMaxPages(view)));
      view.updateView();
   }

   public final boolean switchToPreviousPage(ContainerView view) {
      if (this.isFirstPage(view)) {
         return false;
      }

      this.switchTo(view, this.getCurrentPage(view) - 1);
      return true;
   }

   public final boolean switchToNextPage(ContainerView view) {
      if (this.isLastPage(view)) {
         return false;
      }

      this.switchTo(view, this.getCurrentPage(view) + 1);
      return true;
   }

   @Override
   public List<ClickAction> getDisplayActions() {
      throw new NotImplementedException("AsyncPaginatedList don't support getDisplayActions");
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext renderViewContext) {
      throw new NotImplementedException("AsyncPaginatedList don't support getRenderDisplay");
   }

   @Generated
   public PagingData getPaging() {
      return this.paging;
   }

   @Generated
   public VirtualItem.VirtualItemConsumer getFallbackItem() {
      return this.fallbackItem;
   }

   @Generated
   public void setFallbackItem(VirtualItem.VirtualItemConsumer fallbackItem) {
      this.fallbackItem = fallbackItem;
   }
}
