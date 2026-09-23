package id.seria.gacha.managers.players;

import id.seria.gacha.api.player.PlayerDbModel;
import id.seria.gacha.lib.common.services.services.database.context.ModelIdentifier;
import id.seria.gacha.lib.common.services.services.database.context.annotations.DatabaseField;
import id.seria.gacha.lib.common.services.services.database.context.annotations.DatabaseTable;
import id.seria.gacha.lib.common.services.services.database.context.annotations.keys.PrimaryKey;
import id.seria.gacha.lib.common.services.services.database.context.annotations.keys.Unique;
import lombok.Generated;

@DatabaseTable(name = "%prefix%_player_table")
public class PlayerDatabaseModelImpl implements ModelIdentifier, PlayerDbModel {
   private transient Object lock;
   @PrimaryKey
   @DatabaseField(autoIncrement = true)
   private int id;
   @Unique
   @DatabaseField
   private String uniqueId;
   @DatabaseField
   private String playerName;
   @DatabaseField(size = Integer.MAX_VALUE)
   private String openedCratesAmount;
   @DatabaseField(size = Integer.MAX_VALUE)
   private String openedPouchesAmount;
   @DatabaseField(size = Integer.MAX_VALUE)
   private String cratesCooldown;
   @DatabaseField(size = Integer.MAX_VALUE)
   private String rewardsWinAmount;
   @DatabaseField(size = Integer.MAX_VALUE)
   private String rewardsCooldown;
   @DatabaseField(size = Integer.MAX_VALUE)
   private String virtualKeys;
   @DatabaseField(size = Integer.MAX_VALUE)
   private String rerolls;

   public PlayerDatabaseModelImpl(int id) {
      this.id = id;
   }

   @Override
   public String toString() {
      return String.format(
         "PlayerDatabaseModel[id=%d, playerName=%s, uniqueId=%s, openedCratesAmount=%s, openedPouchesAmount=%s, cratesCooldown=%s, rewardsWinAmount=%s, rewardsCooldown=%s, virtualKeys=%s, rerolls=%s]",
         this.id,
         this.uniqueId,
         this.playerName,
         this.openedCratesAmount,
         this.openedPouchesAmount,
         this.cratesCooldown,
         this.rewardsWinAmount,
         this.rewardsCooldown,
         this.virtualKeys,
         this.rerolls
      );
   }

   @Override
   public String getItemsToClaim() {
      return null;
   }

   @Override
   public void setItemsToClaim(String itemsToClaim) {
   }

   @Generated
   @Override
   public Object getLock() {
      return this.lock;
   }

   @Generated
   @Override
   public int getId() {
      return this.id;
   }

   @Generated
   @Override
   public String getUniqueId() {
      return this.uniqueId;
   }

   @Generated
   @Override
   public String getPlayerName() {
      return this.playerName;
   }

   @Generated
   @Override
   public String getOpenedCratesAmount() {
      return this.openedCratesAmount;
   }

   @Generated
   @Override
   public String getOpenedPouchesAmount() {
      return this.openedPouchesAmount;
   }

   @Generated
   @Override
   public String getCratesCooldown() {
      return this.cratesCooldown;
   }

   @Generated
   @Override
   public String getRewardsWinAmount() {
      return this.rewardsWinAmount;
   }

   @Generated
   @Override
   public String getRewardsCooldown() {
      return this.rewardsCooldown;
   }

   @Generated
   @Override
   public String getVirtualKeys() {
      return this.virtualKeys;
   }

   @Generated
   @Override
   public String getRerolls() {
      return this.rerolls;
   }

   @Generated
   @Override
   public void setLock(Object lock) {
      this.lock = lock;
   }

   @Generated
   @Override
   public void setId(int id) {
      this.id = id;
   }

   @Generated
   @Override
   public void setUniqueId(String uniqueId) {
      this.uniqueId = uniqueId;
   }

   @Generated
   @Override
   public void setPlayerName(String playerName) {
      this.playerName = playerName;
   }

   @Generated
   @Override
   public void setOpenedCratesAmount(String openedCratesAmount) {
      this.openedCratesAmount = openedCratesAmount;
   }

   @Generated
   @Override
   public void setOpenedPouchesAmount(String openedPouchesAmount) {
      this.openedPouchesAmount = openedPouchesAmount;
   }

   @Generated
   @Override
   public void setCratesCooldown(String cratesCooldown) {
      this.cratesCooldown = cratesCooldown;
   }

   @Generated
   @Override
   public void setRewardsWinAmount(String rewardsWinAmount) {
      this.rewardsWinAmount = rewardsWinAmount;
   }

   @Generated
   @Override
   public void setRewardsCooldown(String rewardsCooldown) {
      this.rewardsCooldown = rewardsCooldown;
   }

   @Generated
   @Override
   public void setVirtualKeys(String virtualKeys) {
      this.virtualKeys = virtualKeys;
   }

   @Generated
   @Override
   public void setRerolls(String rerolls) {
      this.rerolls = rerolls;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof PlayerDatabaseModelImpl other)) {
         return false;
      } else {
         if (!other.canEqual(this)) {
            return false;
         }

         if (this.getId() != other.getId()) {
            return false;
         }

         Object this$uniqueId = this.getUniqueId();
         Object other$uniqueId = other.getUniqueId();
         if (this$uniqueId == null ? other$uniqueId == null : this$uniqueId.equals(other$uniqueId)) {
            Object this$playerName = this.getPlayerName();
            Object other$playerName = other.getPlayerName();
            if (this$playerName == null ? other$playerName == null : this$playerName.equals(other$playerName)) {
               Object this$openedCratesAmount = this.getOpenedCratesAmount();
               Object other$openedCratesAmount = other.getOpenedCratesAmount();
               if (this$openedCratesAmount == null ? other$openedCratesAmount == null : this$openedCratesAmount.equals(other$openedCratesAmount)) {
                  Object this$openedPouchesAmount = this.getOpenedPouchesAmount();
                  Object other$openedPouchesAmount = other.getOpenedPouchesAmount();
                  if (this$openedPouchesAmount == null ? other$openedPouchesAmount == null : this$openedPouchesAmount.equals(other$openedPouchesAmount)) {
                     Object this$cratesCooldown = this.getCratesCooldown();
                     Object other$cratesCooldown = other.getCratesCooldown();
                     if (this$cratesCooldown == null ? other$cratesCooldown == null : this$cratesCooldown.equals(other$cratesCooldown)) {
                        Object this$rewardsWinAmount = this.getRewardsWinAmount();
                        Object other$rewardsWinAmount = other.getRewardsWinAmount();
                        if (this$rewardsWinAmount == null ? other$rewardsWinAmount == null : this$rewardsWinAmount.equals(other$rewardsWinAmount)) {
                           Object this$rewardsCooldown = this.getRewardsCooldown();
                           Object other$rewardsCooldown = other.getRewardsCooldown();
                           if (this$rewardsCooldown == null ? other$rewardsCooldown == null : this$rewardsCooldown.equals(other$rewardsCooldown)) {
                              Object this$virtualKeys = this.getVirtualKeys();
                              Object other$virtualKeys = other.getVirtualKeys();
                              if (this$virtualKeys == null ? other$virtualKeys == null : this$virtualKeys.equals(other$virtualKeys)) {
                                 Object this$rerolls = this.getRerolls();
                                 Object other$rerolls = other.getRerolls();
                                 return this$rerolls == null ? other$rerolls == null : this$rerolls.equals(other$rerolls);
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof PlayerDatabaseModelImpl;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getId();
      Object $uniqueId = this.getUniqueId();
      result = result * 59 + ($uniqueId == null ? 43 : $uniqueId.hashCode());
      Object $playerName = this.getPlayerName();
      result = result * 59 + ($playerName == null ? 43 : $playerName.hashCode());
      Object $openedCratesAmount = this.getOpenedCratesAmount();
      result = result * 59 + ($openedCratesAmount == null ? 43 : $openedCratesAmount.hashCode());
      Object $openedPouchesAmount = this.getOpenedPouchesAmount();
      result = result * 59 + ($openedPouchesAmount == null ? 43 : $openedPouchesAmount.hashCode());
      Object $cratesCooldown = this.getCratesCooldown();
      result = result * 59 + ($cratesCooldown == null ? 43 : $cratesCooldown.hashCode());
      Object $rewardsWinAmount = this.getRewardsWinAmount();
      result = result * 59 + ($rewardsWinAmount == null ? 43 : $rewardsWinAmount.hashCode());
      Object $rewardsCooldown = this.getRewardsCooldown();
      result = result * 59 + ($rewardsCooldown == null ? 43 : $rewardsCooldown.hashCode());
      Object $virtualKeys = this.getVirtualKeys();
      result = result * 59 + ($virtualKeys == null ? 43 : $virtualKeys.hashCode());
      Object $rerolls = this.getRerolls();
      return result * 59 + ($rerolls == null ? 43 : $rerolls.hashCode());
   }

   @Generated
   public PlayerDatabaseModelImpl(
      Object lock,
      int id,
      String uniqueId,
      String playerName,
      String openedCratesAmount,
      String openedPouchesAmount,
      String cratesCooldown,
      String rewardsWinAmount,
      String rewardsCooldown,
      String virtualKeys,
      String rerolls
   ) {
      this.lock = lock;
      this.id = id;
      this.uniqueId = uniqueId;
      this.playerName = playerName;
      this.openedCratesAmount = openedCratesAmount;
      this.openedPouchesAmount = openedPouchesAmount;
      this.cratesCooldown = cratesCooldown;
      this.rewardsWinAmount = rewardsWinAmount;
      this.rewardsCooldown = rewardsCooldown;
      this.virtualKeys = virtualKeys;
      this.rerolls = rerolls;
   }

   @Generated
   public PlayerDatabaseModelImpl() {
   }
}
