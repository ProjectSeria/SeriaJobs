package id.seria.gacha.lib.common.nms.interfaces.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockExpEvent;
import org.jetbrains.annotations.NotNull;

public class BlockBreakEventCP extends BlockExpEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private boolean dropItems;
   private boolean cancel;

   public BlockBreakEventCP(@NotNull Block theBlock, @NotNull Player player) {
      super(theBlock, 0);
      this.player = player;
      this.dropItems = true;
   }

   @NotNull
   public Player getPlayer() {
      return this.player;
   }

   public void setDropItems(boolean dropItems) {
      this.dropItems = dropItems;
   }

   public boolean isDropItems() {
      return this.dropItems;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean cancel) {
      this.cancel = cancel;
   }

   @com.avaje.ebean.validation.NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @com.avaje.ebean.validation.NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }
}
