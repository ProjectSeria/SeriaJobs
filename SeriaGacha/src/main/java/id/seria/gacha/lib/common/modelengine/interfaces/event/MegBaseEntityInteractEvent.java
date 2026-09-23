package id.seria.gacha.lib.common.modelengine.interfaces.event;

import com.avaje.ebean.validation.NotNull;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegBaseEntity;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class MegBaseEntityInteractEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final MegBaseEntity baseEntity;
   private final MegActiveModel model;
   private final MegBaseEntityInteractEvent.Action action;
   private final EquipmentSlot slot;
   private final boolean isSecondary;
   private final ItemStack item;
   private final Vector clickedPosition;

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public MegBaseEntity getBaseEntity() {
      return this.baseEntity;
   }

   @Generated
   public MegActiveModel getModel() {
      return this.model;
   }

   @Generated
   public MegBaseEntityInteractEvent.Action getAction() {
      return this.action;
   }

   @Generated
   public EquipmentSlot getSlot() {
      return this.slot;
   }

   @Generated
   public boolean isSecondary() {
      return this.isSecondary;
   }

   @Generated
   public ItemStack getItem() {
      return this.item;
   }

   @Generated
   public Vector getClickedPosition() {
      return this.clickedPosition;
   }

   @Generated
   public MegBaseEntityInteractEvent(
      Player player,
      MegBaseEntity baseEntity,
      MegActiveModel model,
      MegBaseEntityInteractEvent.Action action,
      EquipmentSlot slot,
      boolean isSecondary,
      ItemStack item,
      Vector clickedPosition
   ) {
      this.player = player;
      this.baseEntity = baseEntity;
      this.model = model;
      this.action = action;
      this.slot = slot;
      this.isSecondary = isSecondary;
      this.item = item;
      this.clickedPosition = clickedPosition;
   }

   public enum Action {
      ATTACK,
      INTERACT,
      INTERACT_ON;

      Action() {
      }
   }
}
