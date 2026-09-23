package id.seria.gacha.lib.common.nms.interfaces.events;

import lombok.Generated;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class LegacyPlayerInteractAtEntityEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final Entity clickedEntity;
   private final Vector clickedPosition;
   private final EquipmentSlot slot;
   private boolean cancelled;

   public LegacyPlayerInteractAtEntityEvent(Player player, Entity clickedEntity, Vector clickedPosition, EquipmentSlot slot) {
      this.player = player;
      this.clickedEntity = clickedEntity;
      this.clickedPosition = clickedPosition.clone();
      this.slot = slot;
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
   public Entity getClickedEntity() {
      return this.clickedEntity;
   }

   @Generated
   public Vector getClickedPosition() {
      return this.clickedPosition;
   }

   @Generated
   public EquipmentSlot getSlot() {
      return this.slot;
   }
}
