package id.seria.gacha.api.crate.events;

import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.reward.BaseReward;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class CrateRewardWinItemsEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   protected final CrateInstance crate;
   protected final BaseReward reward;
   protected final Player player;
   protected List<ItemStack> winItems;

   public CrateRewardWinItemsEvent(CrateInstance crate, BaseReward reward, Player player, List<ItemStack> winItems) {
      this.crate = crate;
      this.reward = reward;
      this.player = player;
      this.winItems = winItems;
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

   @Generated
   public List<ItemStack> getWinItems() {
      return this.winItems;
   }

   @Generated
   public void setWinItems(List<ItemStack> winItems) {
      this.winItems = winItems;
   }
}
