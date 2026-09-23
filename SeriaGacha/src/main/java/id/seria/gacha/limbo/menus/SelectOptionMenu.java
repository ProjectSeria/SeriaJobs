package id.seria.gacha.limbo.menus;

import com.google.common.collect.Lists;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class SelectOptionMenu<T> extends CustomInventory {
   private final List<SelectOptionMenu.OptionItem<T>> items;
   private final int itemRows;
   private final Consumer<T> consumer;
   protected Consumer<InventoryClickEvent> backConsumer;
   protected InventoryHolder cachedHolder;

   public SelectOptionMenu(ServerPlugin plugin, String title, List<SelectOptionMenu.OptionItem<T>> items, Consumer<T> consumer) {
      this(plugin, title, items, 3, consumer);
   }

   public SelectOptionMenu(ServerPlugin plugin, String title, List<SelectOptionMenu.OptionItem<T>> items, int itemRows, Consumer<T> consumer) {
      super(plugin, title, 1 + itemRows);
      this.items = items;
      this.itemRows = itemRows;
      this.consumer = consumer;
      this.backConsumer = event -> {};
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      CustomInventory.PaginationWrapper holder = (CustomInventory.PaginationWrapper)inventory.getHolder();
      int maxItemsPerPage = this.itemRows * 7;
      int maxPages = (int)Math.ceil((double)this.items.size() / maxItemsPerPage);
      ItemStack glass = ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a72").build();
      this.fillBottomBorder(glass);
      this.setItem(
         5,
         this.getRows(),
         ItemBuilder.of(XMaterial.FEATHER).setDisplayName("\u00a76" + Translations.t("labels.go-back")[0]),
         event -> this.backConsumer.accept(event)
      );
      int paginationY = 2;
      if (this.itemRows == 5) {
         paginationY = 3;
      }

      if (holder.getCurrentPage() != 1) {
         int previousPage = holder.getCurrentPage() - 1;
         this.setItem(
            1,
            paginationY,
            ItemBuilder.of(XMaterial.ARROW)
               .setDisplayName(Translations.t("layout.extras.previous-page.title", "%page%", previousPage)[0])
               .setLore(Translations.t("layout.extras.previous-page.description")),
            __ -> {
               holder.setCurrentPage(holder.getCurrentPage() - 1);
               this.updateInventorySync(player);
            }
         );
      }

      if (holder.getCurrentPage() != maxPages && maxPages != 1) {
         int nextPage = holder.getCurrentPage() + 1;
         this.setItem(
            9,
            paginationY,
            ItemBuilder.of(XMaterial.ARROW)
               .setDisplayName(Translations.t("layout.extras.next-page.title", "%page%", nextPage)[0])
               .setLore(Translations.t("layout.extras.next-page.description")),
            __ -> {
               holder.setCurrentPage(holder.getCurrentPage() + 1);
               this.updateInventorySync(player);
            }
         );
      }

      int i = (holder.getCurrentPage() - 1) * maxItemsPerPage;

      for (int y = 1; y <= this.itemRows; y++) {
         for (int x = 2; x <= 8; x++) {
            if (i >= this.items.size()) {
               return;
            }

            SelectOptionMenu.OptionItem<T> option = this.items.get(i);
            this.setItem(x, y, ItemBuilder.of(option.display).setDisplayName(option.displayName).setLore(option.lore), __ -> {
               player.closeInventory();
               this.consumer.accept(option.object);
            });
            i++;
         }
      }
   }

   @Override
   public void open(Player player) {
      this.open(player, new CustomInventory.PaginationWrapper(this));
   }

   @Generated
   public List<SelectOptionMenu.OptionItem<T>> getItems() {
      return this.items;
   }

   @Generated
   public int getItemRows() {
      return this.itemRows;
   }

   @Generated
   public Consumer<T> getConsumer() {
      return this.consumer;
   }

   @Generated
   public Consumer<InventoryClickEvent> getBackConsumer() {
      return this.backConsumer;
   }

   @Generated
   public InventoryHolder getCachedHolder() {
      return this.cachedHolder;
   }

   @Generated
   public void setBackConsumer(Consumer<InventoryClickEvent> backConsumer) {
      this.backConsumer = backConsumer;
   }

   @Generated
   public void setCachedHolder(InventoryHolder cachedHolder) {
      this.cachedHolder = cachedHolder;
   }

   public static class OptionItem<T> {
      private final T object;
      private final ItemStack display;
      private final String displayName;
      private final List<String> lore;

      public OptionItem(T object, ItemStack display, String displayName, List<String> lore) {
         this.object = object;
         this.display = display;
         this.displayName = displayName;
         this.lore = lore;
      }

      public OptionItem(T object, XMaterial material, String displayName, List<String> lore) {
         this.object = object;
         this.display = material.parseItem();
         this.displayName = displayName;
         this.lore = lore;
      }

      public OptionItem(T object, XMaterial material, String displayName, String[] lore) {
         this.object = object;
         this.display = material.parseItem();
         this.displayName = displayName;
         this.lore = Lists.newArrayList(lore);
      }

      @Generated
      public T getObject() {
         return this.object;
      }

      @Generated
      public ItemStack getDisplay() {
         return this.display;
      }

      @Generated
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }
   }
}
