package id.seria.gacha.lib.common.nms.interfaces.events;

import com.avaje.ebean.validation.NotNull;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class LegacyBlockExplodeEvent extends BlockEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancel;
   private final List<Block> blocks;
   private float yield;

   public LegacyBlockExplodeEvent(Block what, List<Block> blocks, float yield) {
      super(what);
      this.blocks = blocks;
      this.yield = yield;
      this.cancel = false;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean cancel) {
      this.cancel = cancel;
   }

   @NotNull
   public List<Block> blockList() {
      return this.blocks;
   }

   public float getYield() {
      return this.yield;
   }

   public void setYield(float yield) {
      this.yield = yield;
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
