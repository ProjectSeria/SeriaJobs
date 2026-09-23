package id.seria.gacha.lib.common.nms.interfaces.events;

import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class LegacyPlayerSwapHandItemsEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private ItemStack mainHandItem;
   private ItemStack offHandItem;
   private boolean cancelled;

   public LegacyPlayerSwapHandItemsEvent(Player player, ItemStack mainHandItem, ItemStack offHandItem) {
      this.player = player;
      this.mainHandItem = mainHandItem;
      this.offHandItem = offHandItem;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public ItemStack getMainHandItem() {
      return this.mainHandItem;
   }

   @Generated
   public ItemStack getOffHandItem() {
      return this.offHandItem;
   }

   @Generated
   public void setMainHandItem(ItemStack mainHandItem) {
      this.mainHandItem = mainHandItem;
   }

   @Generated
   public void setOffHandItem(ItemStack offHandItem) {
      this.offHandItem = offHandItem;
   }
}
