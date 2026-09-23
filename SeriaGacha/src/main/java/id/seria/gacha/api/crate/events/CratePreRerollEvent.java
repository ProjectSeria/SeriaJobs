package id.seria.gacha.api.crate.events;

import id.seria.gacha.api.session.OpeningSession;
import lombok.Generated;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CratePreRerollEvent extends Event implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   private final OpeningSession session;
   private boolean cancelled;
   private boolean handled;

   public CratePreRerollEvent(OpeningSession session) {
      this.session = session;
      this.cancelled = false;
      this.handled = false;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @Generated
   public OpeningSession getSession() {
      return this.session;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public boolean isHandled() {
      return this.handled;
   }

   @Generated
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Generated
   public void setHandled(boolean handled) {
      this.handled = handled;
   }
}
