package id.seria.gacha.api.crate.events;

import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.crate.CrateType;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CratePreOpenEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   protected final CrateInstance crate;
   protected final Player player;
   protected boolean cancelled;

   public CratePreOpenEvent(CrateInstance crate, Player player) {
      this.crate = crate;
      this.player = player;
   }

   public CrateType getType() {
      return this.crate.getType();
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
   public CrateInstance getCrate() {
      return this.crate;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
