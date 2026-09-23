package id.seria.gacha.api.crate.events;

import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.reward.BaseReward;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CrateRerollEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   protected final CrateInstance crate;
   protected final BaseReward reward;
   protected final Player player;

   public CrateRerollEvent(CrateInstance crate, BaseReward reward, Player player) {
      this.crate = crate;
      this.reward = reward;
      this.player = player;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @Generated
   public CrateInstance getCrate() {
      return this.crate;
   }

   @Generated
   public BaseReward getReward() {
      return this.reward;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
