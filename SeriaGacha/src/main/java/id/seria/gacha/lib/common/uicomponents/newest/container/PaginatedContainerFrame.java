package id.seria.gacha.lib.common.uicomponents.newest.container;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.PlaceholderUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.core.locales.providers.PluginTranslationsProvider;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.entity.Player;

@Deprecated
public abstract class PaginatedContainerFrame<T> extends ContainerLayout {
   public static String CURRENT_PAGE_KEY = "legacy.paginated-container-frame.current_page";
   public static String MAX_PAGES_KEY = "legacy.paginated-container-frame.max_pages";
   public static String ITEMS_KEY = "legacy.paginated-container-frame.items";
   private PaginationSettings settings;
   private final List<T> items = new ArrayList<>();
   private VirtualItem.VirtualItemConsumer fallbackItem = (ctx, view) -> view.withItem(XMaterial.AIR);
   private VirtualItem.VirtualItemConsumer previousPageItem;
   private VirtualItem.VirtualItemConsumer nextPageItem;

   @Deprecated
   public PaginatedContainerFrame(ServerPlugin plugin, PluginTranslationsProvider translationProvider, PaginationSettings settings) {
      super(plugin, translationProvider, Translatable.literal(""), null);
      this.settings = settings;
   }

   @Override
   public void onPrepare(ContainerView view) {
      int maxItemsPerPage = this.settings.getCompound().length();
      int maxPages = this.items.isEmpty() ? 1 : (int)Math.ceil((double)this.countMaxItems() / maxItemsPerPage);
      view.setMetadata(String.format(MAX_PAGES_KEY, this.id), maxPages, true);
   }

   public abstract int countMaxItems();

   @Override
   public void onRender(RenderViewContext context) {
      ContainerView view = context.getContainerView();
      if (!this.getItems().isEmpty()) {
         if (!this.isFirstPage(view) && this.previousPageItem != null) {
            VirtualItem item = new VirtualItem();
            this.previousPageItem.accept(context, item);
            if (item.getSlots()[0] == VirtualItem.UNDEFINED) {
               throw new NotImplementedException("Previous Page Navigation Item was not defined, use #setPreviousPageItem");
            }

            item.withDisplayName(
               PlaceholderUtil.replacePlaceholders(this.getPlugin(), new String[]{item.getDisplayName()}, "%page%", this.getPreviousPage(view))[0]
            );
            item.withLore(PlaceholderUtil.replacePlaceholders(this.getPlugin(), item.getLore(), "%page%", this.getPreviousPage(view)));
            Consumer<ClickViewContext> oldConsumer = item.getClickConsumer();
            item.whenClick(ctx -> {
               ctx.getContainerView().getEventBus().publish("previous-page");
               this.switchToPreviousPage(view);
               oldConsumer.accept(ctx);
            });
            item.render(context);
         }

         if (!this.isLastPage(view) && this.nextPageItem != null) {
            VirtualItem item = new VirtualItem();
            this.nextPageItem.accept(context, item);
            if (item.getSlots()[0] == VirtualItem.UNDEFINED) {
               throw new NotImplementedException("Previous Page Navigation Item was not defined, use #setNextPageItem");
            }

            item.withDisplayName(
               PlaceholderUtil.replacePlaceholders(this.getPlugin(), new String[]{item.getDisplayName()}, "%page%", this.getNextPage(view))[0]
            );
            item.withLore(PlaceholderUtil.replacePlaceholders(this.getPlugin(), item.getLore(), "%page%", this.getNextPage(view)));
            Consumer<ClickViewContext> oldConsumer = item.getClickConsumer();
            item.whenClick(ctx -> {
               ctx.getContainerView().getEventBus().publish("next-page");
               this.switchToNextPage(view);
               oldConsumer.accept(ctx);
            });
            item.render(context);
         }
      }

      int startIndex = (this.getCurrentPage(view) - 1) * this.getItemsPerPage();
      int i = startIndex;

      for (int[] group : this.settings.getCompound().horizontalGroups()) {
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
            int occupied = MathUtil.clamp(this.items.size() - i, 0, usable);
            if (occupied != 0) {
               int offset;
               switch (this.settings.getAlign()) {
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
                  T item = this.items.get(i);
                  VirtualItem virtualItem = new VirtualItem();
                  this.onRenderItem(view, i, virtualItem, item);
                  virtualItem.withSlot(position[0], position[1]);
                  context.setSlot(position[0], position[1], virtualItem.getItem(), virtualItem.getClickConsumer());
                  i++;
               }
            }
         }
      }
   }

   @Override
   public void open(Player viewer) {
      this.open(new ContainerView(this, viewer));
   }

   public abstract void onRenderItem(ContainerView var1, int var2, VirtualItem var3, T var4);

   public void setItems(List<T> items) {
      synchronized (items) {
         this.items.clear();
         this.items.addAll(items);
      }
   }

   public int getItemsPerPage() {
      return this.settings.getCompound().asArray().length;
   }

   public final int getMaxItems() {
      synchronized (this.items) {
         return this.items.size();
      }
   }

   public int getCurrentPage(ContainerView view) {
      return view.getMetadata(String.format(CURRENT_PAGE_KEY), 1);
   }

   public int setCurrentPage(ContainerView view, int currentPage) {
      view.setMetadata(String.format(CURRENT_PAGE_KEY), currentPage, true);
      return currentPage;
   }

   public int getMaxPages(ContainerView view) {
      return view.getMetadata(String.format(MAX_PAGES_KEY), 1);
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

   @Generated
   public PaginationSettings getSettings() {
      return this.settings;
   }

   @Generated
   public List<T> getItems() {
      return this.items;
   }

   @Generated
   public VirtualItem.VirtualItemConsumer getFallbackItem() {
      return this.fallbackItem;
   }

   @Generated
   public VirtualItem.VirtualItemConsumer getPreviousPageItem() {
      return this.previousPageItem;
   }

   @Generated
   public VirtualItem.VirtualItemConsumer getNextPageItem() {
      return this.nextPageItem;
   }

   @Generated
   public void setSettings(PaginationSettings settings) {
      this.settings = settings;
   }

   @Generated
   public void setFallbackItem(VirtualItem.VirtualItemConsumer fallbackItem) {
      this.fallbackItem = fallbackItem;
   }

   @Generated
   public void setPreviousPageItem(VirtualItem.VirtualItemConsumer previousPageItem) {
      this.previousPageItem = previousPageItem;
   }

   @Generated
   public void setNextPageItem(VirtualItem.VirtualItemConsumer nextPageItem) {
      this.nextPageItem = nextPageItem;
   }
}
