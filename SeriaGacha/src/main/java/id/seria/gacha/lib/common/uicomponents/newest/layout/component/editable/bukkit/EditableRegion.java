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
import id.seria.gacha.lib.common.utils.objects.Region;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.xseries.XSound;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Generated;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

public class EditableRegion extends EditableObject<Region> {
   private Map<UUID, Listener> cachedListeners = new HashMap<>();
   private Function<Block, Boolean> firstPositionValidator;
   private Translatable firstPositionValidatorError;

   @Deprecated
   public EditableRegion(ComponentData data, Supplier<Region> object) {
      this(new SlotCompound(data.getSlot()), data, object.get());
   }

   public EditableRegion(SlotCompound slot, ComponentData data, MutableData<Region> mutableData) {
      super(slot, data, null, mutableData);
      this.setClickActions();
   }

   public EditableRegion(SlotCompound slot, ComponentData data, Region object) {
      super(slot, data, null, object);
      this.setClickActions();
   }

   public EditableRegion addFirstPositionValidator(Function<Block, Boolean> validator, Translatable error) {
      this.firstPositionValidator = validator;
      this.firstPositionValidatorError = error;
      return this;
   }

   private void setClickActions() {
      this.setClickAction(
         ClickAction.SET(ClickAction.ClickType.LEFT, Translatable.key("labels.region")),
         context -> {
            final Player viewer = context.getViewer();
            viewer.closeInventory();
            if (!this.cachedListeners.containsKey(viewer.getUniqueId())) {
               final Task task = this.getPlugin().getScheduler().runSync(() -> {
                  if (viewer.isOnline() && this.cachedListeners.containsKey(viewer.getUniqueId())) {
                     viewer.sendMessage("\u00a7eRegion editing session expired.");
                     Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7a", 0, 10, 0);
                     this.cachedListeners.remove(viewer.getUniqueId());
                     TextReader.cancel(viewer.getUniqueId());
                  }
               }, 2400L);
               Listener listener = new Listener() {
                  private Block first;
                  private Block second;

                  @EventHandler
                  public void onPlayerInteract(LegacyPlayerInteractEvent event) {
                     if (this.first == null || this.second == null) {
                        if (event.getHand() == EquipmentSlot.HAND && event.getClickedBlock() != null) {
                           Player player = event.getPlayer();
                           if (viewer.equals(player)) {
                              event.setCancelled(true);
                              if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                                 if (this.first != null) {
                                    player.sendMessage("\u00a7cYou have already registered the first position, set the second one now. (Right-Click)");
                                    return;
                                 }

                                 Block attemptedFirst = event.getClickedBlock();
                                 if (EditableRegion.this.firstPositionValidator != null && !EditableRegion.this.firstPositionValidator.apply(attemptedFirst)) {
                                    String message = EditableRegion.this._t(EditableRegion.this.firstPositionValidatorError)[0];
                                    if (message != null && !message.isEmpty()) {
                                       player.sendMessage(new String[]{"\u00a7cAn error has occurred:", message});
                                    }

                                    player.sendMessage("\u00a7eLeft-Click a different block to try again.");
                                    return;
                                 }

                                 this.first = attemptedFirst;
                                 player.sendMessage("\u00a7aThe first position has been registered.");
                                 player.playSound(player.getLocation(), XSound.ENTITY_ARROW_HIT_PLAYER.parseSound(), 1.0F, 1.0F);
                                 Utilities.getNMSFactory()
                                    .sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7aRight-Click to define the second position", 0, 2400, 0);
                              } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                                 if (this.first == null) {
                                    player.sendMessage("\u00a7cYou haven't registered the first position yet. (Left-Click)");
                                    return;
                                 }

                                 Block attemptedSecond = event.getClickedBlock();

                                 try {
                                    EditableRegion.this.setValue(Region.from(this.first, attemptedSecond));
                                    this.second = attemptedSecond;
                                    player.sendMessage("\u00a7aThe second position has been registered.");
                                    player.playSound(player.getLocation(), XSound.ENTITY_ARROW_HIT_PLAYER.parseSound(), 1.0F, 1.0F);
                                    context.getContainerView().getContainer().open(EditableRegion.this.getContainerView());
                                    task.cancel();
                                    HandlerList.unregisterAll(this);
                                    EditableRegion.this.cachedListeners.remove(viewer.getUniqueId());
                                    TextReader.cancel(viewer.getUniqueId());
                                    Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7a", 0, 10, 0);
                                 } catch (Exception e) {
                                    if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                                       viewer.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
                                    }

                                    viewer.sendMessage("\u00a7eRight-Click a different block to try again.");
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
                     task.cancel();
                     this.cachedListeners.remove(viewer.getUniqueId());
                     HandlerList.unregisterAll(listener);
                     viewer.sendMessage("\u00a7eRegion editing session canceled.");
                     Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7a", 0, 10, 0);
                     context.getContainerView().getContainer().open(this.getContainerView());
                  }
               });
               viewer.sendMessage("\u00a7aYou have just joined a session to set a region.");
               viewer.sendMessage("\u00a7e\"Left-Click\" \u00a7aand \u00a7e\"Right-Click\" \u00a7aon blocks to set the region.");
               Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(viewer), "\u00a7a", "\u00a7aLeft-Click to define the first position", 0, 2400, 0);
            }
         }
      );
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return this.getValue().get() == null
         ? this.translate(Translatable.key("layout.displays.region.empty"))
         : this.translate(
            Translatable.key(
               "layout.displays.region.populated",
               "%world%",
               this.getValue().get().getWorld().getName(),
               "%minimum%",
               this.getValue().get().getMinimumPosition(),
               "%maximum%",
               this.getValue().get().getMaximumPosition()
            )
         );
   }

   private List<String> translate(Translatable key) {
      return Arrays.asList(super._t(key));
   }

   @Generated
   public Map<UUID, Listener> getCachedListeners() {
      return this.cachedListeners;
   }

   @Generated
   public Function<Block, Boolean> getFirstPositionValidator() {
      return this.firstPositionValidator;
   }

   @Generated
   public Translatable getFirstPositionValidatorError() {
      return this.firstPositionValidatorError;
   }
}
