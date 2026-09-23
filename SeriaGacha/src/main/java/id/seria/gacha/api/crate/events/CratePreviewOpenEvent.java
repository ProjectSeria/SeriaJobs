package id.seria.gacha.api.crate.events;

import id.seria.gacha.api.crate.CrateType;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CratePreviewOpenEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   protected final CrateType crateType;
   protected final String previewMenu;
   protected final Player player;
   protected boolean cancelled;

   public CratePreviewOpenEvent(CrateType crateType, String previewMenu, Player player) {
      this.crateType = crateType;
      this.previewMenu = previewMenu;
      this.player = player;
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
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public String getPreviewMenu() {
      return this.previewMenu;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
