package id.seria.gacha.api.crate.events;

import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.reward.BaseReward;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CrateSelectiveConfirmEvent extends Event implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   private final CrateInstance crate;
   private final Player player;
   private final BaseReward selectedReward;
   private boolean cancelled;

   public CrateSelectiveConfirmEvent(CrateInstance crate, Player player, BaseReward selectedReward) {
      this.crate = crate;
      this.player = player;
      this.selectedReward = selectedReward;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @Generated
   public CrateInstance getCrate() {
      return this.crate;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public BaseReward getSelectedReward() {
      return this.selectedReward;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
