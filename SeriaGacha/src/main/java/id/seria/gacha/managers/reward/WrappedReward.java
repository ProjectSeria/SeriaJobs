package id.seria.gacha.managers.reward;

import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import java.util.List;
import lombok.Generated;

public class WrappedReward implements BaseReward {
   private final String identifier;
   private final Item displayItem;
   private final List<? extends Item> winItems;
   private final List<String> winCommands;
   private final double weight;
   private final boolean broadcast;
   private final boolean sharePlayersOnline;
   private final boolean allowWinMessage;
   private final RewardMessageType rewardMessageType;
   private boolean alternative;
   private int maxWinItemAmount;
   private int maxWinCommandAmount;

   public WrappedReward(
      String identifier,
      Item displayItem,
      List<? extends Item> winItems,
      List<String> winCommands,
      double weight,
      boolean broadcast,
      boolean sharePlayersOnline,
      boolean allowWinMessage,
      RewardMessageType rewardMessageType,
      boolean alternative
   ) {
      this(identifier, displayItem, winItems, winCommands, weight, broadcast, sharePlayersOnline, allowWinMessage, rewardMessageType, alternative, -1, -1);
   }

   public WrappedReward(
      String identifier,
      Item displayItem,
      List<? extends Item> winItems,
      List<String> winCommands,
      double weight,
      boolean broadcast,
      boolean sharePlayersOnline,
      boolean allowWinMessage,
      RewardMessageType rewardMessageType,
      boolean alternative,
      int maxWinItemAmount,
      int maxWinCommandAmount
   ) {
      this.identifier = identifier;
      this.displayItem = displayItem;
      this.winItems = winItems;
      this.winCommands = winCommands;
      this.weight = weight;
      this.broadcast = broadcast;
      this.sharePlayersOnline = sharePlayersOnline;
      this.allowWinMessage = allowWinMessage;
      this.rewardMessageType = rewardMessageType;
      this.alternative = alternative;
      this.maxWinItemAmount = maxWinItemAmount;
      this.maxWinCommandAmount = maxWinCommandAmount;
   }

   @Override
   public boolean isAllowWinMessage() {
      return this.allowWinMessage;
   }

   @Override
   public double getWeight() {
      return this.weight;
   }

   @Deprecated
   public Item getItemStack() {
      return this.displayItem;
   }

   @Deprecated
   public String getDisplayName() {
      return BukkitUtil.extractName(this.displayItem.build());
   }

   @Generated
   public void setAlternative(boolean alternative) {
      this.alternative = alternative;
   }

   @Generated
   public void setMaxWinItemAmount(int maxWinItemAmount) {
      this.maxWinItemAmount = maxWinItemAmount;
   }

   @Generated
   public void setMaxWinCommandAmount(int maxWinCommandAmount) {
      this.maxWinCommandAmount = maxWinCommandAmount;
   }

   @Generated
   @Override
   public String getIdentifier() {
      return this.identifier;
   }

   @Generated
   @Override
   public Item getDisplayItem() {
      return this.displayItem;
   }

   @Generated
   @Override
   public List<? extends Item> getWinItems() {
      return this.winItems;
   }

   @Generated
   @Override
   public List<String> getWinCommands() {
      return this.winCommands;
   }

   @Generated
   @Override
   public boolean isBroadcast() {
      return this.broadcast;
   }

   @Generated
   @Override
   public boolean isSharePlayersOnline() {
      return this.sharePlayersOnline;
   }

   @Generated
   public RewardMessageType getRewardMessageType() {
      return this.rewardMessageType;
   }

   @Generated
   @Override
   public boolean isAlternative() {
      return this.alternative;
   }

   @Generated
   @Override
   public int getMaxWinItemAmount() {
      return this.maxWinItemAmount;
   }

   @Generated
   @Override
   public int getMaxWinCommandAmount() {
      return this.maxWinCommandAmount;
   }
}
