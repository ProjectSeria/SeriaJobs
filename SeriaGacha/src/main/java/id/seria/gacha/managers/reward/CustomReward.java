package id.seria.gacha.managers.reward;

import id.seria.gacha.Crates;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.reward.Reward;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.PlayerUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.crates.rewards.GlobalRewardStatsTracker;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.registries.InformationRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomReward extends Configuration implements Reward, Cloneable {
   public static long ID_COUNTER = System.currentTimeMillis();
   public static ItemStack INVALID_REWARD = ItemBuilder.of(XMaterial.BARRIER)
      .setDisplayName("\u00a7cInvalid Reward")
      .setLore("\u00a77This reward is invalid, make", "\u00a77sure to check if display item or", "\u00a77content items are set.")
      .build();
   @ConfigField
   private boolean enabled = true;
   protected boolean registered = true;
   @ConfigField
   protected String identifier = "reward_" + ID_COUNTER++;
   @ConfigField(path = "display-item", legacyPaths = "item")
   protected BaseItem displayItem = BaseItem.vanilla(XMaterial.DIAMOND_PICKAXE, "&aReward Example");
   @ConfigField(legacyPaths = "items")
   protected List<Item> winItems = new ArrayList<>();
   @ConfigField(legacyPaths = "commands")
   protected List<String> winCommands = new ArrayList<>();
   @ConfigField
   protected boolean broadcast;
   @ConfigField(legacyPaths = "percentage")
   protected double weight = 100.0;
   @ConfigField
   protected int winLimits;
   @ConfigField(path = "global-win-limit")
   protected int globalWinLimit;
   @ConfigField(path = "cooldowns.win-limit-cooldown")
   protected long winLimitCooldown;
   @ConfigField(path = "cooldowns.global-win-limit-cooldown")
   protected long globalWinLimitCooldown;
   @ConfigField(path = "cooldowns.time-limited-cooldown")
   protected long timeLimitedCooldown;
   @ConfigField(path = "cooldowns.time-limited-expiry")
   protected long timeLimitedExpiry;
   @ConfigField
   protected int guaranteedWin;
   @ConfigField
   protected int requiredKeys;
   @ConfigField(path = "allow-win-message")
   protected boolean allowWinMessage;
   @ConfigField(path = "notification-type")
   protected RewardMessageType rewardMessageType;
   @ConfigField
   protected boolean sharePlayersOnline;
   @ConfigField(legacyPaths = "blacklist-permissions")
   protected List<String> restrictedPermissions;
   @ConfigField(path = "required-permissions")
   protected List<String> requiredPermissions;
   @ConfigField(path = "max-win.item-amount")
   protected int maxWinItemAmount;
   @ConfigField(path = "max-win.command-amount")
   protected int maxWinCommandAmount;
   @ConfigField(path = "alternative-reward")
   protected CustomAlternativeReward alternative;

   public CustomReward(
      boolean enabled,
      boolean registered,
      String identifier,
      BaseItem displayItem,
      List<Item> winItems,
      double weight,
      boolean broadcast,
      boolean allowWinMessage,
      boolean sharePlayersOnline,
      List<String> winCommands,
      int winLimits,
      int globalWinLimit,
      long winLimitCooldown,
      long globalWinLimitCooldown,
      long timeLimitedCooldown,
      long timeLimitedExpiry,
      int guaranteedWin,
      int requiredKeys,
      List<String> restrictedPermissions,
      CustomAlternativeReward alternative
   ) {
      this.broadcast = false;
      this.allowWinMessage = true;
      this.rewardMessageType = RewardMessageType.CHAT;
      this.sharePlayersOnline = false;
      this.winLimits = -1;
      this.globalWinLimit = -1;
      this.winLimitCooldown = 0L;
      this.globalWinLimitCooldown = 0L;
      this.timeLimitedCooldown = 0L;
      this.timeLimitedExpiry = 0L;
      this.guaranteedWin = -1;
      this.restrictedPermissions = new ArrayList<>();
      this.requiredPermissions = new ArrayList<>();
      this.maxWinItemAmount = -1;
      this.maxWinCommandAmount = -1;
      this.alternative = new CustomAlternativeReward();
      this.enabled = enabled;
      this.registered = registered;
      this.identifier = identifier;
      this.displayItem = displayItem;
      this.winItems = winItems;
      this.weight = weight;
      this.broadcast = broadcast;
      this.allowWinMessage = allowWinMessage;
      this.sharePlayersOnline = sharePlayersOnline;
      this.winCommands = winCommands;
      this.winLimits = winLimits;
      this.globalWinLimit = globalWinLimit;
      this.winLimitCooldown = winLimitCooldown;
      this.globalWinLimitCooldown = globalWinLimitCooldown;
      this.timeLimitedCooldown = timeLimitedCooldown;
      this.timeLimitedExpiry = timeLimitedExpiry;
      this.guaranteedWin = guaranteedWin;
      this.requiredKeys = requiredKeys;
      this.restrictedPermissions = restrictedPermissions;
      this.alternative = alternative;
   }

   public BaseItem getDisplayItem() {
      return this.displayItem != null && this.displayItem.getType() != Material.AIR ? this.displayItem : null;
   }

   @Override
   public int getRequiredKeys() {
      Integer rarityKeys = InformationRegistry.getValue(this, Addons.RARITY_REQUIRED_KEYS, Integer.class);
      return rarityKeys != null && rarityKeys > 0 ? rarityKeys : Math.max(this.requiredKeys, 1);
   }

   public void markRegistered() {
      this.registered = true;
   }

   public void unmarkRegistered() {
      this.registered = false;
   }

   public boolean isValid() {
      return this.isDisplayValid();
   }

   public boolean isDisplayValid() {
      return this.displayItem != null && this.displayItem.getType() != Material.AIR;
   }

   public boolean isTimeLimitedExpired() {
      return this.timeLimitedExpiry > 0L && System.currentTimeMillis() >= this.timeLimitedExpiry;
   }

   public CustomReward clone() {
      CustomReward clone = new CustomReward(
         this.enabled,
         this.registered,
         this.identifier,
         this.displayItem.clone(),
         this.winItems.stream().map(Item::clone).collect(Collectors.toList()),
         this.weight,
         this.broadcast,
         this.allowWinMessage,
         this.sharePlayersOnline,
         new ArrayList<>(this.winCommands),
         this.winLimits,
         this.globalWinLimit,
         this.winLimitCooldown,
         this.globalWinLimitCooldown,
         this.timeLimitedCooldown,
         this.timeLimitedExpiry,
         this.guaranteedWin,
         this.requiredKeys,
         new ArrayList<>(this.restrictedPermissions),
         this.alternative.clone()
      );
      clone.rewardMessageType = this.rewardMessageType;
      clone.requiredPermissions = new ArrayList<>(this.requiredPermissions);
      clone.maxWinItemAmount = this.maxWinItemAmount;
      clone.maxWinCommandAmount = this.maxWinCommandAmount;
      this.copyLoadedModulesTo(clone);
      return clone;
   }

   public boolean isBlacklistRestricted(Player player) {
      return !player.isOp() && PlayerUtil.haveSomePermission(player, this.restrictedPermissions);
   }

   public boolean isRequiredPermissionMissing(Player player) {
      return !this.requiredPermissions.isEmpty() && this.requiredPermissions.stream().noneMatch(player::hasPermission);
   }

   @Override
   public boolean isRestricted(Player player) {
      return this.isBlacklistRestricted(player) || this.isRequiredPermissionMissing(player);
   }

   public boolean isWinLimitReached(Player player) {
      if (this.winLimits <= 0) {
         return false;
      }

      PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(player);
      long cooldown = this.getWinLimitCooldown();
      return cooldown > 0L && System.currentTimeMillis() - playerData.getRewardCooldown(this.identifier) >= cooldown * 1000L
         ? false
         : playerData.getRewardWinAmount(this.identifier) >= this.winLimits;
   }

   public boolean isGlobalWinLimitReached(String crateTypeIdentifier) {
      if (this.globalWinLimit <= 0) {
         return false;
      }

      GlobalRewardStatsTracker tracker = Crates.getGlobalRewardStatsTracker();
      long cooldown = this.getGlobalWinLimitCooldown();
      return cooldown > 0L && System.currentTimeMillis() - tracker.getLastWinTimestamp(crateTypeIdentifier, this.identifier) >= cooldown * 1000L
         ? false
         : tracker.getWinAmount(crateTypeIdentifier, this.identifier) >= this.globalWinLimit;
   }

   public boolean isObtainedBy(Player player, String crateTypeIdentifier) {
      return this.isBlacklistRestricted(player) || this.isWinLimitReached(player) || this.isGlobalWinLimitReached(crateTypeIdentifier);
   }

   private static boolean isPremiumFeaturesLocked() {
      return Crates.getPlugin().getEdition() == Edition.LITE;
   }

   @Override
   public boolean isSharePlayersOnline() {
      return !isPremiumFeaturesLocked() && this.sharePlayersOnline;
   }

   public long getWinLimitCooldown() {
      return isPremiumFeaturesLocked() ? 0L : this.winLimitCooldown;
   }

   public long getGlobalWinLimitCooldown() {
      return isPremiumFeaturesLocked() ? 0L : this.globalWinLimitCooldown;
   }

   @Override
   public int getMaxWinItemAmount() {
      return isPremiumFeaturesLocked() ? -1 : this.maxWinItemAmount;
   }

   @Override
   public int getMaxWinCommandAmount() {
      return isPremiumFeaturesLocked() ? -1 : this.maxWinCommandAmount;
   }

   @Generated
   @Override
   public String toString() {
      return "CustomReward(enabled="
         + this.isEnabled()
         + ", registered="
         + this.isRegistered()
         + ", identifier="
         + this.getIdentifier()
         + ", displayItem="
         + this.getDisplayItem()
         + ", winItems="
         + this.getWinItems()
         + ", winCommands="
         + this.getWinCommands()
         + ", broadcast="
         + this.isBroadcast()
         + ", weight="
         + this.getWeight()
         + ", winLimits="
         + this.getWinLimits()
         + ", globalWinLimit="
         + this.getGlobalWinLimit()
         + ", winLimitCooldown="
         + this.getWinLimitCooldown()
         + ", globalWinLimitCooldown="
         + this.getGlobalWinLimitCooldown()
         + ", timeLimitedCooldown="
         + this.getTimeLimitedCooldown()
         + ", timeLimitedExpiry="
         + this.getTimeLimitedExpiry()
         + ", guaranteedWin="
         + this.getGuaranteedWin()
         + ", requiredKeys="
         + this.getRequiredKeys()
         + ", allowWinMessage="
         + this.isAllowWinMessage()
         + ", rewardMessageType="
         + this.getRewardMessageType()
         + ", sharePlayersOnline="
         + this.isSharePlayersOnline()
         + ", restrictedPermissions="
         + this.getRestrictedPermissions()
         + ", requiredPermissions="
         + this.getRequiredPermissions()
         + ", maxWinItemAmount="
         + this.getMaxWinItemAmount()
         + ", maxWinCommandAmount="
         + this.getMaxWinCommandAmount()
         + ", alternative="
         + this.getAlternative()
         + ")";
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public boolean isRegistered() {
      return this.registered;
   }

   @Generated
   @Override
   public String getIdentifier() {
      return this.identifier;
   }

   @Generated
   @Override
   public List<Item> getWinItems() {
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
   public double getWeight() {
      return this.weight;
   }

   @Generated
   @Override
   public int getWinLimits() {
      return this.winLimits;
   }

   @Generated
   public int getGlobalWinLimit() {
      return this.globalWinLimit;
   }

   @Generated
   public long getTimeLimitedCooldown() {
      return this.timeLimitedCooldown;
   }

   @Generated
   public long getTimeLimitedExpiry() {
      return this.timeLimitedExpiry;
   }

   @Generated
   public int getGuaranteedWin() {
      return this.guaranteedWin;
   }

   @Generated
   @Override
   public boolean isAllowWinMessage() {
      return this.allowWinMessage;
   }

   @Generated
   public RewardMessageType getRewardMessageType() {
      return this.rewardMessageType;
   }

   @Generated
   @Override
   public List<String> getRestrictedPermissions() {
      return this.restrictedPermissions;
   }

   @Generated
   public List<String> getRequiredPermissions() {
      return this.requiredPermissions;
   }

   @Generated
   public CustomAlternativeReward getAlternative() {
      return this.alternative;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public void setRegistered(boolean registered) {
      this.registered = registered;
   }

   @Generated
   public void setIdentifier(String identifier) {
      this.identifier = identifier;
   }

   @Generated
   public void setDisplayItem(BaseItem displayItem) {
      this.displayItem = displayItem;
   }

   @Generated
   public void setWinItems(List<Item> winItems) {
      this.winItems = winItems;
   }

   @Generated
   public void setWinCommands(List<String> winCommands) {
      this.winCommands = winCommands;
   }

   @Generated
   public void setBroadcast(boolean broadcast) {
      this.broadcast = broadcast;
   }

   @Generated
   public void setWeight(double weight) {
      this.weight = weight;
   }

   @Generated
   public void setWinLimits(int winLimits) {
      this.winLimits = winLimits;
   }

   @Generated
   public void setGlobalWinLimit(int globalWinLimit) {
      this.globalWinLimit = globalWinLimit;
   }

   @Generated
   public void setWinLimitCooldown(long winLimitCooldown) {
      this.winLimitCooldown = winLimitCooldown;
   }

   @Generated
   public void setGlobalWinLimitCooldown(long globalWinLimitCooldown) {
      this.globalWinLimitCooldown = globalWinLimitCooldown;
   }

   @Generated
   public void setTimeLimitedCooldown(long timeLimitedCooldown) {
      this.timeLimitedCooldown = timeLimitedCooldown;
   }

   @Generated
   public void setTimeLimitedExpiry(long timeLimitedExpiry) {
      this.timeLimitedExpiry = timeLimitedExpiry;
   }

   @Generated
   public void setGuaranteedWin(int guaranteedWin) {
      this.guaranteedWin = guaranteedWin;
   }

   @Generated
   public void setRequiredKeys(int requiredKeys) {
      this.requiredKeys = requiredKeys;
   }

   @Generated
   public void setAllowWinMessage(boolean allowWinMessage) {
      this.allowWinMessage = allowWinMessage;
   }

   @Generated
   public void setRewardMessageType(RewardMessageType rewardMessageType) {
      this.rewardMessageType = rewardMessageType;
   }

   @Generated
   public void setSharePlayersOnline(boolean sharePlayersOnline) {
      this.sharePlayersOnline = sharePlayersOnline;
   }

   @Generated
   public void setRestrictedPermissions(List<String> restrictedPermissions) {
      this.restrictedPermissions = restrictedPermissions;
   }

   @Generated
   public void setRequiredPermissions(List<String> requiredPermissions) {
      this.requiredPermissions = requiredPermissions;
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
   public void setAlternative(CustomAlternativeReward alternative) {
      this.alternative = alternative;
   }

   @Generated
   public CustomReward(
      boolean enabled,
      boolean registered,
      String identifier,
      BaseItem displayItem,
      List<Item> winItems,
      List<String> winCommands,
      boolean broadcast,
      double weight,
      int winLimits,
      int globalWinLimit,
      long winLimitCooldown,
      long globalWinLimitCooldown,
      long timeLimitedCooldown,
      long timeLimitedExpiry,
      int guaranteedWin,
      int requiredKeys,
      boolean allowWinMessage,
      RewardMessageType rewardMessageType,
      boolean sharePlayersOnline,
      List<String> restrictedPermissions,
      List<String> requiredPermissions,
      int maxWinItemAmount,
      int maxWinCommandAmount,
      CustomAlternativeReward alternative
   ) {
      this.broadcast = false;
      this.allowWinMessage = true;
      this.rewardMessageType = RewardMessageType.CHAT;
      this.sharePlayersOnline = false;
      this.winLimits = -1;
      this.globalWinLimit = -1;
      this.winLimitCooldown = 0L;
      this.globalWinLimitCooldown = 0L;
      this.timeLimitedCooldown = 0L;
      this.timeLimitedExpiry = 0L;
      this.guaranteedWin = -1;
      this.restrictedPermissions = new ArrayList<>();
      this.requiredPermissions = new ArrayList<>();
      this.maxWinItemAmount = -1;
      this.maxWinCommandAmount = -1;
      this.alternative = new CustomAlternativeReward();
      this.enabled = enabled;
      this.registered = registered;
      this.identifier = identifier;
      this.displayItem = displayItem;
      this.winItems = winItems;
      this.winCommands = winCommands;
      this.broadcast = broadcast;
      this.weight = weight;
      this.winLimits = winLimits;
      this.globalWinLimit = globalWinLimit;
      this.winLimitCooldown = winLimitCooldown;
      this.globalWinLimitCooldown = globalWinLimitCooldown;
      this.timeLimitedCooldown = timeLimitedCooldown;
      this.timeLimitedExpiry = timeLimitedExpiry;
      this.guaranteedWin = guaranteedWin;
      this.requiredKeys = requiredKeys;
      this.allowWinMessage = allowWinMessage;
      this.rewardMessageType = rewardMessageType;
      this.sharePlayersOnline = sharePlayersOnline;
      this.restrictedPermissions = restrictedPermissions;
      this.requiredPermissions = requiredPermissions;
      this.maxWinItemAmount = maxWinItemAmount;
      this.maxWinCommandAmount = maxWinCommandAmount;
      this.alternative = alternative;
   }

   @Generated
   public CustomReward() {
      this.broadcast = false;
      this.allowWinMessage = true;
      this.rewardMessageType = RewardMessageType.CHAT;
      this.sharePlayersOnline = false;
      this.winLimits = -1;
      this.globalWinLimit = -1;
      this.winLimitCooldown = 0L;
      this.globalWinLimitCooldown = 0L;
      this.timeLimitedCooldown = 0L;
      this.timeLimitedExpiry = 0L;
      this.guaranteedWin = -1;
      this.restrictedPermissions = new ArrayList<>();
      this.requiredPermissions = new ArrayList<>();
      this.maxWinItemAmount = -1;
      this.maxWinCommandAmount = -1;
      this.alternative = new CustomAlternativeReward();
   }
}
