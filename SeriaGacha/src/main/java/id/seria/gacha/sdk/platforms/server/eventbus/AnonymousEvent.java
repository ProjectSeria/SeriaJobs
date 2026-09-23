package id.seria.gacha.sdk.platforms.server.eventbus;

import lombok.Generated;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AnonymousEvent extends Event {
   private Object value;
   private static final HandlerList handlers = new HandlerList();

   public AnonymousEvent(Object value) {
      this.value = value;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @Generated
   public Object getValue() {
      return this.value;
   }
}
