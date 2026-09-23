package id.seria.gacha.lib.common.utils.inventory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@Deprecated
public abstract class CustomInventory {
   private static Map<UUID, CustomInventory> openedInventories = Maps.newHashMap();
   private static Multimap<ServerPlugin, CustomInventory> inventoriesInstance = ArrayListMultimap.create();
   protected final ServerPlugin plugin;
   private String title;
   private final int rows;
   private final int size;
   private ItemStack[] stacks;
   private Consumer<InventoryClickEvent>[] consumers;
   public Consumer<Player> onPlayerCloseInventory;
   private final Set<Player> viewers;
   private Consumer<Player> setupConsumer;
   private boolean canPickupInventoryItems;
   private boolean canPickupMenuItems;

   public static void register(ServerPlugin plugin) {
      plugin.getEventBus()
         .registerListener(
            new Listener() {
               @EventHandler(priority = EventPriority.LOW)
               protected void onClick(InventoryClickEvent event) {
                  Player player = (Player)event.getWhoClicked();
                  Inventory inventory = player.getOpenInventory().getTopInventory();
                  if (event.getClickedInventory() != null && inventory != null) {
                     InventoryHolder holder = inventory.getHolder();
                     if (holder instanceof CustomInventory.HolderWrapper) {
                        CustomInventory.HolderWrapper wrapper = (CustomInventory.HolderWrapper)holder;
                        CustomInventory sustainer = wrapper.getSustainer();
                        sustainer.onMenuClick(event);
                        if (!event.isCancelled()) {
                           if ((
                                 !player.getOpenInventory().getBottomInventory().equals(event.getClickedInventory())
                                    || !sustainer.canPickupInventoryItems
                                    || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                              )
                              && !sustainer.canPickupMenuItems) {
                              event.setCancelled(true);
                           }

                           if (event.getRawSlot() < sustainer.getSize()) {
                              if (sustainer.getStacks()[event.getRawSlot()] == null) {
                                 return;
                              }

                              event.setCancelled(true);
                           }

                           Consumer<InventoryClickEvent> consumer = sustainer.getConsumers()[event.getRawSlot()];
                           if (consumer != null && event.getCursor() != null && event.getCursor().getType() == Material.AIR) {
                              consumer.accept(event);
                           }
                        }
                     }
                  }
               }

               @EventHandler(priority = EventPriority.LOW)
               protected void onClose(InventoryOpenEvent event) {
                  Inventory inventory = event.getInventory();
                  Player player = (Player)event.getPlayer();
                  if (inventory != null && inventory.getHolder() instanceof CustomInventory.HolderWrapper) {
                     CustomInventory.HolderWrapper wrapper = (CustomInventory.HolderWrapper)inventory.getHolder();
                     CustomInventory sustainer = wrapper.getSustainer();
                     sustainer.getViewers().add(player);
                     CustomInventory.openedInventories.put(player.getUniqueId(), sustainer);
                     CustomInventory.inventoriesInstance.put(sustainer.getPlugin(), sustainer);
                     sustainer.onMenuOpen(event);
                  }
               }

               @EventHandler(priority = EventPriority.LOW)
               protected void onClose(InventoryCloseEvent event) {
                  Inventory inventory = event.getInventory();
                  Player player = (Player)event.getPlayer();
                  if (inventory != null && inventory.getHolder() instanceof CustomInventory.HolderWrapper) {
                     CustomInventory.HolderWrapper wrapper = (CustomInventory.HolderWrapper)inventory.getHolder();
                     CustomInventory sustainer = wrapper.getSustainer();
                     sustainer.getViewers().remove(player);
                     CustomInventory.openedInventories.remove(player.getUniqueId());
                     CustomInventory.inventoriesInstance.remove(sustainer.getPlugin(), sustainer);
                     sustainer.onMenuClose(event);
                  }
               }

               @EventHandler
               protected void onPluginDisable(PluginDisableEvent event) {
                  if (event.getPlugin() instanceof JavaPlugin) {
                     JavaPlugin pluginx = (JavaPlugin)event.getPlugin();
                     CustomInventory.unregister(pluginx);
                  }
               }
            }
         );
   }

   public static void unregister() {
      for (UUID uuid : openedInventories.keySet()) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null) {
            player.closeInventory();
         }
      }
   }

   public static void unregister(JavaPlugin plugin) {
      for (UUID uuid : openedInventories.keySet()) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null) {
            player.closeInventory();
         }
      }
   }

   public CustomInventory(ServerPlugin plugin, String name, int numRows) {
      this.plugin = plugin;
      this.title = name;
      this.rows = numRows;
      this.size = numRows * 9;
      this.stacks = new ItemStack[this.size];
      this.consumers = new Consumer[this.size + 36];
      this.viewers = ConcurrentHashMap.newKeySet();
   }

   public void open(Player player) {
      this.open(player, new CustomInventory.HolderWrapper(this));
   }

   public void open(Player player, CustomInventory.HolderWrapper wrapper) {
      if (!this.getViewers().contains(player)) {
         Utilities.getAsyncService().execute(() -> {
            try {
               Inventory inventory = this.prepareInventory(wrapper, player).get();
               this.plugin.getScheduler().runSync(player, () -> player.openInventory(inventory));
            } catch (Exception e) {
               this.plugin.getLogger().error("Error opening inventory", e);
            }
         });
      }
   }

   public Future<Inventory> prepareInventory(CustomInventory.HolderWrapper wrapper, Player player) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            this.stacks = new ItemStack[this.size];
            this.consumers = new Consumer[this.size + 36];
            String normalizedTitle = this.title;
            CompletableFuture<Inventory> future = new CompletableFuture<>();
            this.getPlugin().getScheduler().runSync(() -> {
               try {
                  Inventory inv = Bukkit.createInventory(wrapper, this.size, normalizedTitle);
                  future.complete(inv);
               } catch (Exception ex) {
                  future.completeExceptionally(ex);
               }
            });
            Inventory inventory = future.get(2L, TimeUnit.SECONDS);
            wrapper.setInventory(inventory);
            this.setup(player, inventory);

            for (int i = 0; i < this.stacks.length; i++) {
               if (this.stacks[i] != null) {
                  inventory.setItem(i, this.stacks[i]);
               }
            }

            return inventory;
         } catch (Exception e) {
            this.plugin.getLogger().error("Error preparing inventory", e);
            return null;
         }
      });
   }

   public abstract void setup(Player var1, Inventory var2);

   public void updateInventorySync(Player player) {
      this.stacks = new ItemStack[this.size];
      this.consumers = new Consumer[this.size + 36];
      Inventory inventory = player.getOpenInventory().getTopInventory();
      if (inventory != null && this.getViewers().contains(player)) {
         this.setup(player, inventory);

         for (int i = 0; i < this.stacks.length; i++) {
            if (this.stacks[i] == null) {
               if (i < inventory.getSize()) {
                  inventory.setItem(i, new ItemStack(Material.AIR));
               }
            } else if (i < inventory.getSize()) {
               inventory.setItem(i, this.stacks[i]);
            }
         }

         player.updateInventory();
      }
   }

   public void updateInventorySync() {
      this.viewers.forEach(player -> this.updateInventorySync(player));
   }

   public void setItem(int x, int y, ItemBuilder builder, Consumer<InventoryClickEvent> consumer) {
      this.setItem(InventoryUtil.getPositionRaw(x, y), builder.build(), consumer);
   }

   public void setItem(int x, int y, ItemStack item, Consumer<InventoryClickEvent> consumer) {
      this.setItem(InventoryUtil.getPositionRaw(x, y), item, consumer);
   }

   public void setItem(int x, int y, ItemBuilder builder) {
      this.setItem(InventoryUtil.getPositionRaw(x, y), builder.build(), null);
   }

   public void setItem(int x, int y, ItemStack item) {
      this.setItem(InventoryUtil.getPositionRaw(x, y), item, null);
   }

   public void setItem(int slot, ItemBuilder builder, Consumer<InventoryClickEvent> consumer) {
      this.setItem(slot, builder.build(), consumer);
   }

   public void setItem(int slot, ItemStack item) {
      this.setItem(slot, item, null);
   }

   public void setItem(int slot, ItemBuilder builder) {
      this.setItem(slot, builder, null);
   }

   public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> consumer) {
      this.consumers[slot] = consumer;
      this.stacks[slot] = item;
   }

   public void fillMenu(ItemBuilder item) {
      this.fillMenu(item.build(), null);
   }

   public void fillMenu(ItemBuilder item, Consumer<InventoryClickEvent> consumer) {
      this.fillMenu(item.build(), consumer);
   }

   public void fillMenu(ItemStack item) {
      this.fillMenu(item, null);
   }

   public void fillMenu(ItemStack item, Consumer<InventoryClickEvent> consumer) {
      for (int slot = 0; slot < this.stacks.length; slot++) {
         if (this.stacks[slot] == null) {
            this.consumers[slot] = consumer;
            this.stacks[slot] = item;
         }
      }
   }

   public void fillBorder(ItemBuilder item) {
      this.fillBorder(item.build(), null);
   }

   public void fillBorder(ItemBuilder item, Consumer<InventoryClickEvent> consumer) {
      this.fillBorder(item.build(), consumer);
   }

   public void fillBorder(ItemStack item) {
      this.fillBorder(item, null);
   }

   public void fillBorder(ItemStack item, Consumer<InventoryClickEvent> consumer) {
      int x = 1;
      int y = 1;

      for (int slot = 0; slot < this.stacks.length; slot++) {
         if (this.stacks[slot] == null && (x == 1 || x == 9 || y == 1 || y == 9)) {
            this.consumers[slot] = consumer;
            this.stacks[slot] = item;
         }

         if (++x > 9) {
            x = 1;
            y++;
         }
      }
   }

   public void fillLeftBorder(ItemStack item) {
      int x = 1;

      for (int slot = 0; slot < this.stacks.length; slot++) {
         if (x == 1) {
            this.stacks[slot] = item;
         }

         if (++x > 9) {
            x = 1;
         }
      }
   }

   public void fillLeftBorder(ItemBuilder item) {
      this.fillLeftBorder(item.build());
   }

   public void fillRightBorder(ItemStack item) {
      int x = 1;

      for (int slot = 0; slot < this.stacks.length; slot++) {
         if (x == 9) {
            this.stacks[slot] = item;
         }

         if (++x > 9) {
            x = 1;
         }
      }
   }

   public void fillRightBorder(ItemBuilder item) {
      this.fillRightBorder(item.build());
   }

   public void fillTopBorder(ItemStack item) {
      for (int i = 0; i < 9; i++) {
         this.stacks[i] = item;
      }
   }

   public void fillBottomBorder(ItemBuilder item) {
      this.fillBottomBorder(item.build());
   }

   public void fillBottomBorder(ItemStack item) {
      for (int x = 1; x <= 9; x++) {
         this.stacks[InventoryUtil.getPositionRaw(x, this.rows)] = item;
      }
   }

   public void fillTopBorder(ItemBuilder item) {
      this.fillTopBorder(item.build());
   }

   public void onMenuOpen(InventoryOpenEvent event) {
   }

   public void onMenuClose(InventoryCloseEvent event) {
   }

   public void onMenuClick(InventoryClickEvent event) {
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public String getTitle() {
      return this.title;
   }

   @Generated
   public int getRows() {
      return this.rows;
   }

   @Generated
   public int getSize() {
      return this.size;
   }

   @Generated
   public ItemStack[] getStacks() {
      return this.stacks;
   }

   @Generated
   public Consumer<InventoryClickEvent>[] getConsumers() {
      return this.consumers;
   }

   @Generated
   public Consumer<Player> getOnPlayerCloseInventory() {
      return this.onPlayerCloseInventory;
   }

   @Generated
   public Set<Player> getViewers() {
      return this.viewers;
   }

   @Generated
   public Consumer<Player> getSetupConsumer() {
      return this.setupConsumer;
   }

   @Generated
   public boolean isCanPickupInventoryItems() {
      return this.canPickupInventoryItems;
   }

   @Generated
   public boolean isCanPickupMenuItems() {
      return this.canPickupMenuItems;
   }

   @Generated
   public static Map<UUID, CustomInventory> getOpenedInventories() {
      return openedInventories;
   }

   @Generated
   public static Multimap<ServerPlugin, CustomInventory> getInventoriesInstance() {
      return inventoriesInstance;
   }

   @Generated
   public void setTitle(String title) {
      this.title = title;
   }

   @Generated
   public void setSetupConsumer(Consumer<Player> setupConsumer) {
      this.setupConsumer = setupConsumer;
   }

   @Generated
   public void setCanPickupInventoryItems(boolean canPickupInventoryItems) {
      this.canPickupInventoryItems = canPickupInventoryItems;
   }

   @Generated
   public void setCanPickupMenuItems(boolean canPickupMenuItems) {
      this.canPickupMenuItems = canPickupMenuItems;
   }

   public static class HolderWrapper implements InventoryHolder {
      private Inventory inventory;
      private final CustomInventory sustainer;

      @Generated
      public Inventory getInventory() {
         return this.inventory;
      }

      @Generated
      public CustomInventory getSustainer() {
         return this.sustainer;
      }

      @Generated
      public void setInventory(Inventory inventory) {
         this.inventory = inventory;
      }

      @Generated
      public HolderWrapper(CustomInventory sustainer) {
         this.sustainer = sustainer;
      }
   }

   public static class PaginationWrapper extends CustomInventory.HolderWrapper {
      private int currentPage = 1;

      public PaginationWrapper(CustomInventory sustainer) {
         super(sustainer);
      }

      @Generated
      public int getCurrentPage() {
         return this.currentPage;
      }

      @Generated
      public void setCurrentPage(int currentPage) {
         this.currentPage = currentPage;
      }
   }
}
