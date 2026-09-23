package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.lib.common.nms.interfaces.events.LegacyPlayerInteractEvent;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.data.MutableData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.xseries.XSound;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class EditableLocation extends EditableObject<Location> {
   private final EditableLocation.ActionType actionType;
   private Map<UUID, Listener> cachedListeners = new HashMap<>();

   @Deprecated
   public EditableLocation(ComponentData data, Supplier<Location> object) {
      this(new SlotCompound(data.getSlot()), data, object.get());
   }

   @Deprecated
   public EditableLocation(ComponentData data, EditableLocation.ActionType actionType, Supplier<Location> object) {
      this(new SlotCompound(data.getSlot()), data, actionType, object.get());
   }

   public EditableLocation(SlotCompound slot, ComponentData data, Location object) {
      this(slot, data, EditableLocation.ActionType.SHIFT_TO_DEFINE, object);
   }

   public EditableLocation(SlotCompound slot, ComponentData data, EditableLocation.ActionType actionType, Location object) {
      this(slot, data, actionType, new MutableData<>(null, object));
   }

   public EditableLocation(SlotCompound slot, ComponentData data, EditableLocation.ActionType actionType, MutableData<Location> mutableData) {
      super(slot, data, null, mutableData);
      this.actionType = actionType;
      this.setClickAction(
         ClickAction.SET(ClickAction.ClickType.LEFT, Translatable.key("labels.location")),
         context -> {
            final Player viewer = context.getViewer();
            viewer.closeInventory();
            if (!this.cachedListeners.containsKey(viewer.getUniqueId())) {
               if (actionType == EditableLocation.ActionType.SHIFT_TO_DEFINE) {
                  Listener listener = new Listener() {
                     private final Task task = context.getContainerView().getPlugin().getScheduler().runSync(() -> {
                        if (viewer.isOnline() && EditableLocation.this.cachedListeners.containsKey(viewer.getUniqueId())) {
                           viewer.sendMessage("\u00a7eLocation editing session expired.");
                           Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7a", 0, 10, 0);
                           EditableLocation.this.cachedListeners.remove(viewer.getUniqueId());
                           TextReader.cancel(viewer.getUniqueId());
                           HandlerList.unregisterAll(this);
                        }
                     }, 2400L);

                     @EventHandler(ignoreCancelled = true)
                     protected void onPlayerInteract(PlayerInteractEvent event) {
                        Player player = event.getPlayer();
                        if (viewer.equals(player)) {
                           if (player.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK) {
                              event.setCancelled(true);
                           }
                        }
                     }

                     @EventHandler(ignoreCancelled = true)
                     protected void onPlayerLeftClick(PlayerAnimationEvent event) {
                        Player player = event.getPlayer();
                        if (viewer.equals(player)) {
                           if (player.isSneaking()) {
                              try {
                                 EditableLocation.this.setValue(viewer.getLocation());
                                 context.getContainerView().getContainer().open(EditableLocation.this.getContainerView());
                                 this.task.cancel();
                                 EditableLocation.this.getPlugin()
                                    .getScheduler()
                                    .runSync(() -> EditableLocation.this.getPlugin().getEventBus().unregisterListener(this), 1L);
                                 player.playSound(player.getLocation(), XSound.ENTITY_ARROW_HIT_PLAYER.parseSound(), 1.0F, 1.0F);
                                 EditableLocation.this.cachedListeners.remove(viewer.getUniqueId());
                                 TextReader.cancel(viewer.getUniqueId());
                                 Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7a", 0, 10, 0);
                                 event.setCancelled(true);
                              } catch (Exception e) {
                                 if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                                    viewer.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
                                 }
                              }
                           }
                        }
                     }
                  };
                  this.getPlugin().getEventBus().registerListener(listener);
                  this.cachedListeners.put(viewer.getUniqueId(), listener);
                  TextReader.askForInput(viewer, message -> {
                     if (this.cachedListeners.containsKey(viewer.getUniqueId())) {
                        this.cachedListeners.remove(viewer.getUniqueId());
                        HandlerList.unregisterAll(listener);
                        viewer.sendMessage("\u00a7eLocation editing session canceled.");
                        Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7a", 0, 10, 0);
                        context.getContainerView().getContainer().open(this.getContainerView());
                     }
                  });
                  viewer.sendMessage("\u00a7aYou have just joined a session to set a location.");
                  viewer.sendMessage("\u00a7e\"Sneak + Left-Click\" \u00a7ato set the location.");
                  Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7aSneak + Left-Click", 0, 2400, 0);
               } else if (actionType == EditableLocation.ActionType.CLICK_TO_DEFINE) {
                  Listener listener = new Listener() {
                     private Location first;
                     private Location second;

                     @EventHandler
                     public void onPlayerInteract(LegacyPlayerInteractEvent event) {
                        if (this.first == null || this.second == null) {
                           if (event.getHand() == EquipmentSlot.HAND && event.getClickedBlock() != null) {
                              Player player = event.getPlayer();
                              if (viewer.equals(player)) {
                                 event.setCancelled(true);

                                 try {
                                    player.sendMessage("\u00a7aBlock position registered.");
                                    player.playSound(player.getLocation(), XSound.ENTITY_ARROW_HIT_PLAYER.parseSound(), 1.0F, 1.0F);
                                    EditableLocation.this.setValue(event.getClickedBlock().getLocation());
                                    context.getContainerView().getContainer().open(viewer);
                                    HandlerList.unregisterAll(this);
                                    EditableLocation.this.cachedListeners.remove(viewer.getUniqueId());
                                    TextReader.cancel(viewer.getUniqueId());
                                    Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7a", 0, 10, 0);
                                 } catch (Exception e) {
                                    if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                                       viewer.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
                                    }
                                 }
                              }
                           }
                        }
                     }
                  };
                  this.getPlugin().getEventBus().registerListener(listener);
                  this.cachedListeners.put(viewer.getUniqueId(), listener);
                  TextReader.askForInput(viewer, message -> {
                     if (this.cachedListeners.containsKey(viewer.getUniqueId())) {
                        this.cachedListeners.remove(viewer.getUniqueId());
                        HandlerList.unregisterAll(listener);
                        viewer.sendMessage("\u00a7eLocation editing session canceled.");
                        Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7a", 0, 10, 0);
                        context.getContainerView().getContainer().open(this.getContainerView());
                     }
                  });
                  viewer.sendMessage("\u00a7aYou have just joined a session to set a location.");
                  viewer.sendMessage("\u00a7e\"Left/Right-Click\" \u00a7aa block to set the location.");
                  Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7aLeft/Right-Click", 0, 2400, 0);
               }
            }
         }
      );
      this.setClickAction(ClickAction.CLEAR(ClickAction.ClickType.RIGHT, Translatable.key("labels.location")), context -> {
         try {
            this.setValue(null);
            context.getContainerView().updateView();
         } catch (Exception e) {
            context.getViewer().sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
         }
      });
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return this.getValue().get() == null
         ? Arrays.asList(this._t(Translatable.key("layout.displays.location.empty")))
         : Arrays.asList(
            this._t(
               Translatable.key(
                  "layout.displays.location.populated",
                  "%world%",
                  this.getValue().get().getWorld().getName(),
                  "%x%",
                  this.getValue().get().getX(),
                  "%y%",
                  this.getValue().get().getY(),
                  "%z%",
                  this.getValue().get().getZ(),
                  "%yaw%",
                  this.getValue().get().getYaw(),
                  "%pitch%",
                  this.getValue().get().getPitch()
               )
            )
         );
   }

   @Generated
   public EditableLocation.ActionType getActionType() {
      return this.actionType;
   }

   @Generated
   public Map<UUID, Listener> getCachedListeners() {
      return this.cachedListeners;
   }

   public enum ActionType {
      SHIFT_TO_DEFINE,
      CLICK_TO_DEFINE;

      ActionType() {
      }
   }
}
