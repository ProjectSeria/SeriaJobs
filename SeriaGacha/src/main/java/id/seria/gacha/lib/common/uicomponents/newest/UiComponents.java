package id.seria.gacha.lib.common.uicomponents.newest;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import id.seria.gacha.lib.common.uicomponents.newest.container.AbstractContainer;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class UiComponents {
   private static final long INTERNAL_CLICK_DELAY_MS = 50L;
   private static Map<UUID, ContainerView> CURRENT_VIEWS = Maps.newHashMap();
   private static Multimap<JavaPlugin, AbstractContainer> REGISTERED_FRAMES = ArrayListMultimap.create();
   private static Map<UUID, Long> LAST_INTERNAL_CLICK = Maps.newHashMap();

   public UiComponents() {
   }

   private static boolean isContainerViewInventory(Inventory inventory) {
      return inventory != null && inventory.getType() == InventoryType.CHEST && inventory.getHolder() instanceof ContainerView;
   }

   public static void enable(ServerPlugin plugin) {
      enable(plugin.getOriginal());
   }

   public static void enable(JavaPlugin plugin) {
      Bukkit.getPluginManager()
         .registerEvents(
            new Listener() {
               @EventHandler(priority = EventPriority.LOW)
               protected void onClick(InventoryClickEvent event) {
                  Player player = (Player)event.getWhoClicked();
                  Inventory inventory = player.getOpenInventory().getTopInventory();
                  if (event.getClickedInventory() != null && UiComponents.isContainerViewInventory(inventory)) {
                     ContainerView holder = (ContainerView)inventory.getHolder();
                     if (event.getRawSlot() >= 0 && event.getRawSlot() < holder.getContainerType().getSize()) {
                        long now = System.currentTimeMillis();
                        Long last = UiComponents.LAST_INTERNAL_CLICK.get(player.getUniqueId());
                        if (last != null && now - last < 50L) {
                           event.setCancelled(true);
                           return;
                        }

                        UiComponents.LAST_INTERNAL_CLICK.put(player.getUniqueId(), now);
                     }

                     ClickViewContext click = new ClickViewContext(player, holder, event);
                     holder.getContainer().onClick(click);
                     if (!event.isCancelled()) {
                        if ((
                              !player.getOpenInventory().getBottomInventory().equals(event.getClickedInventory())
                                 || holder.isCancelOnOtherInventoryClick()
                                 || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                           )
                           && holder.isCancelOnContainerClick()) {
                           event.setCancelled(true);
                        }

                        if (event.getRawSlot() < holder.getContainerType().getSize()) {
                           if (holder.getConsumers()[event.getRawSlot()] == null) {
                              return;
                           }

                           event.setCancelled(true);
                        }

                        Consumer<ClickViewContext> consumer = holder.getConsumers()[event.getRawSlot()];
                        if (consumer != null && event.getCursor() != null && event.getCursor().getType() == Material.AIR) {
                           consumer.accept(click);
                        }
                     }
                  }
               }

               @EventHandler(priority = EventPriority.LOW)
               protected void onClick(InventoryDragEvent event) {
                  Player player = (Player)event.getWhoClicked();
                  Inventory inventory = player.getOpenInventory().getTopInventory();
                  if (event.getInventory() != null && UiComponents.isContainerViewInventory(inventory)) {
                     ContainerView holder = (ContainerView)inventory.getHolder();

                     for (int rawSlot : event.getRawSlots()) {
                        if (rawSlot < inventory.getSize() && holder.isCancelOnContainerClick()) {
                           event.setCancelled(true);
                           break;
                        }

                        if (rawSlot >= inventory.getSize() && holder.isCancelOnOtherInventoryClick()) {
                           event.setCancelled(true);
                           break;
                        }
                     }
                  }
               }

               @EventHandler(priority = EventPriority.LOW)
               protected void onOpen(InventoryOpenEvent event) {
                  Inventory inventory = event.getInventory();
                  Player player = (Player)event.getPlayer();
                  if (UiComponents.isContainerViewInventory(inventory)) {
                     ContainerView holder = (ContainerView)inventory.getHolder();
                     UiComponents.CURRENT_VIEWS.put(player.getUniqueId(), holder);
                     UiComponents.REGISTERED_FRAMES.put(holder.getPlugin().getOriginal(), holder.getContainer());
                  }
               }

               @EventHandler(priority = EventPriority.LOW)
               protected void onClose(InventoryCloseEvent event) {
                  Inventory inventory = event.getInventory();
                  Player player = (Player)event.getPlayer();
                  if (UiComponents.isContainerViewInventory(inventory)) {
                     ContainerView view = (ContainerView)inventory.getHolder();
                     view.setClosed(true);
                     UiComponents.CURRENT_VIEWS.remove(player.getUniqueId());
                     UiComponents.LAST_INTERNAL_CLICK.remove(player.getUniqueId());
                     UiComponents.REGISTERED_FRAMES.remove(view.getPlugin().getOriginal(), view.getContainer());
                     view.getContainer().getViewersContext().remove(player.getUniqueId());
                     if (view.getContainer().isSkipNextOnCloseEvent()) {
                        view.getContainer().setSkipNextOnCloseEvent(false);
                     } else {
                        view.getContainer().onClose(new CloseViewContext(player, view, event));
                     }
                  }
               }

               @EventHandler
               protected void onPluginDisable(PluginDisableEvent event) {
                  if (event.getPlugin() instanceof JavaPlugin) {
                     JavaPlugin pluginx = (JavaPlugin)event.getPlugin();
                     UiComponents.disable(pluginx);
                  }
               }
            },
            plugin
         );
   }

   public static void disable() {
      for (UUID uuid : CURRENT_VIEWS.keySet()) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null) {
            player.closeInventory();
         }
      }

      CURRENT_VIEWS.clear();
      REGISTERED_FRAMES.clear();
      LAST_INTERNAL_CLICK.clear();
   }

   public static void disable(JavaPlugin plugin) {
      for (AbstractContainer frame : REGISTERED_FRAMES.removeAll(plugin)) {
         for (UUID uuid : frame.getViewersContext().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
               player.closeInventory();
            }

            CURRENT_VIEWS.remove(uuid);
            LAST_INTERNAL_CLICK.remove(uuid);
         }

         frame.getViewersContext().clear();
      }
   }

   public static ContainerView getPlayerView(Player player) {
      return CURRENT_VIEWS.get(player.getUniqueId());
   }

   public static ContainerView getPlayerView(UUID uniqueId) {
      return CURRENT_VIEWS.get(uniqueId);
   }
}
