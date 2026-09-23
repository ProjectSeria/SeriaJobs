package id.seria.gacha.lib.common.modelengine.interfaces.event;

import com.avaje.ebean.validation.NotNull;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MegModelRegistrationEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final MegModelRegistrationEvent.Phase phase;

   public MegModelRegistrationEvent(MegModelRegistrationEvent.Phase phase) {
      super(!Bukkit.isPrimaryThread());
      this.phase = phase;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @Generated
   public MegModelRegistrationEvent.Phase getPhase() {
      return this.phase;
   }

   public enum Phase {
      PRE,
      POST,
      FINAL;

      Phase() {
      }
   }
}
