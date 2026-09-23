package id.seria.gacha.api.crate.events;

import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.key.Key;
import id.seria.gacha.api.reward.BaseReward;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CrateRewardSelectionEvent extends Event {
   private static final HandlerList HANDLERS = new HandlerList();
   private final CrateInstance crate;
   private final Player player;
   private final Key key;
   private final List<? extends BaseReward> candidates;
   private BaseReward selectedReward;
   private boolean handled;

   public CrateRewardSelectionEvent(CrateInstance crate, Player player, Key key, List<? extends BaseReward> candidates) {
      this.crate = crate;
      this.player = player;
      this.key = key;
      this.candidates = candidates;
      this.selectedReward = null;
      this.handled = false;
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
   public Key getKey() {
      return this.key;
   }

   @Generated
   public List<? extends BaseReward> getCandidates() {
      return this.candidates;
   }

   @Generated
   public BaseReward getSelectedReward() {
      return this.selectedReward;
   }

   @Generated
   public boolean isHandled() {
      return this.handled;
   }

   @Generated
   public void setSelectedReward(BaseReward selectedReward) {
      this.selectedReward = selectedReward;
   }

   @Generated
   public void setHandled(boolean handled) {
      this.handled = handled;
   }
}
