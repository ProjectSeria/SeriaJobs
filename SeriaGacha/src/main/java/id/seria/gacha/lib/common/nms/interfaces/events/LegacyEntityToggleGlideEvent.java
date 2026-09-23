package id.seria.gacha.lib.common.nms.interfaces.events;

import com.avaje.ebean.validation.NotNull;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class LegacyEntityToggleGlideEvent extends EntityEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancel = false;
   private final boolean isGliding;

   public LegacyEntityToggleGlideEvent(Entity who, boolean isGliding) {
      super(who);
      this.isGliding = isGliding;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean cancel) {
      this.cancel = cancel;
   }

   public boolean isGliding() {
      return this.isGliding;
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
