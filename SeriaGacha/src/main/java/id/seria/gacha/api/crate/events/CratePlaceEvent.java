package id.seria.gacha.api.crate.events;

import id.seria.gacha.api.crate.CrateType;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CratePlaceEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   protected final CrateType crate;
   protected final Player player;
   protected final Location location;
   protected boolean cancelled;

   public CratePlaceEvent(CrateType crate, Player player, Location location) {
      this.crate = crate;
      this.player = player;
      this.location = location;
   }

   public Player getWhoPlaced() {
      return this.player;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @Generated
   public CrateType getCrate() {
      return this.crate;
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }
}
