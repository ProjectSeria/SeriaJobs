package id.seria.gacha.lib.common.nms.interfaces.events;

import com.avaje.ebean.validation.NotNull;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class LegacyEntityResurrectEvent extends EntityEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled;

   public LegacyEntityResurrectEvent(LivingEntity what) {
      super(what);
   }

   @NotNull
   public LivingEntity getEntity() {
      return (LivingEntity)this.entity;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }
}
