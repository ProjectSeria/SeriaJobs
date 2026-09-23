package id.seria.gacha.managers.crates.rewards;

import id.seria.gacha.lib.common.services.services.database.context.ModelIdentifier;
import id.seria.gacha.lib.common.services.services.database.context.annotations.DatabaseField;
import id.seria.gacha.lib.common.services.services.database.context.annotations.DatabaseTable;
import id.seria.gacha.lib.common.services.services.database.context.annotations.keys.PrimaryKey;
import id.seria.gacha.lib.common.services.services.database.context.annotations.keys.Unique;
import lombok.Generated;

@DatabaseTable(name = "%prefix%_reward_global_stats")
public class RewardGlobalStatsModel implements ModelIdentifier {
   private transient Object lock;
   @PrimaryKey
   @DatabaseField(autoIncrement = true)
   private int id;
   @Unique
   @DatabaseField(size = 191)
   private String compositeIdentifier;
   @DatabaseField(size = 191)
   private String crateIdentifier;
   @DatabaseField(size = 191)
   private String rewardIdentifier;
   @DatabaseField
   private int winAmount;
   @DatabaseField
   private long lastWinTimestamp;

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
   public String getCompositeIdentifier() {
      return this.compositeIdentifier;
   }

   @Generated
   public String getCrateIdentifier() {
      return this.crateIdentifier;
   }

   @Generated
   public String getRewardIdentifier() {
      return this.rewardIdentifier;
   }

   @Generated
   public int getWinAmount() {
      return this.winAmount;
   }

   @Generated
   public long getLastWinTimestamp() {
      return this.lastWinTimestamp;
   }

   @Generated
   public void setLock(Object lock) {
      this.lock = lock;
   }

   @Generated
   @Override
   public void setId(int id) {
      this.id = id;
   }

   @Generated
   public void setCompositeIdentifier(String compositeIdentifier) {
      this.compositeIdentifier = compositeIdentifier;
   }

   @Generated
   public void setCrateIdentifier(String crateIdentifier) {
      this.crateIdentifier = crateIdentifier;
   }

   @Generated
   public void setRewardIdentifier(String rewardIdentifier) {
      this.rewardIdentifier = rewardIdentifier;
   }

   @Generated
   public void setWinAmount(int winAmount) {
      this.winAmount = winAmount;
   }

   @Generated
   public void setLastWinTimestamp(long lastWinTimestamp) {
      this.lastWinTimestamp = lastWinTimestamp;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof RewardGlobalStatsModel other)) {
         return false;
      } else {
         if (!other.canEqual(this)) {
            return false;
         }

         if (this.getId() != other.getId()) {
            return false;
         }

         if (this.getWinAmount() != other.getWinAmount()) {
            return false;
         }

         if (this.getLastWinTimestamp() != other.getLastWinTimestamp()) {
            return false;
         }

         Object this$compositeIdentifier = this.getCompositeIdentifier();
         Object other$compositeIdentifier = other.getCompositeIdentifier();
         if (this$compositeIdentifier == null ? other$compositeIdentifier == null : this$compositeIdentifier.equals(other$compositeIdentifier)) {
            Object this$crateIdentifier = this.getCrateIdentifier();
            Object other$crateIdentifier = other.getCrateIdentifier();
            if (this$crateIdentifier == null ? other$crateIdentifier == null : this$crateIdentifier.equals(other$crateIdentifier)) {
               Object this$rewardIdentifier = this.getRewardIdentifier();
               Object other$rewardIdentifier = other.getRewardIdentifier();
               return this$rewardIdentifier == null ? other$rewardIdentifier == null : this$rewardIdentifier.equals(other$rewardIdentifier);
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
      return other instanceof RewardGlobalStatsModel;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getId();
      result = result * 59 + this.getWinAmount();
      long $lastWinTimestamp = this.getLastWinTimestamp();
      result = result * 59 + (int)($lastWinTimestamp >>> 32 ^ $lastWinTimestamp);
      Object $compositeIdentifier = this.getCompositeIdentifier();
      result = result * 59 + ($compositeIdentifier == null ? 43 : $compositeIdentifier.hashCode());
      Object $crateIdentifier = this.getCrateIdentifier();
      result = result * 59 + ($crateIdentifier == null ? 43 : $crateIdentifier.hashCode());
      Object $rewardIdentifier = this.getRewardIdentifier();
      return result * 59 + ($rewardIdentifier == null ? 43 : $rewardIdentifier.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "RewardGlobalStatsModel(lock="
         + this.getLock()
         + ", id="
         + this.getId()
         + ", compositeIdentifier="
         + this.getCompositeIdentifier()
         + ", crateIdentifier="
         + this.getCrateIdentifier()
         + ", rewardIdentifier="
         + this.getRewardIdentifier()
         + ", winAmount="
         + this.getWinAmount()
         + ", lastWinTimestamp="
         + this.getLastWinTimestamp()
         + ")";
   }

   @Generated
   public RewardGlobalStatsModel(
      Object lock, int id, String compositeIdentifier, String crateIdentifier, String rewardIdentifier, int winAmount, long lastWinTimestamp
   ) {
      this.lock = lock;
      this.id = id;
      this.compositeIdentifier = compositeIdentifier;
      this.crateIdentifier = crateIdentifier;
      this.rewardIdentifier = rewardIdentifier;
      this.winAmount = winAmount;
      this.lastWinTimestamp = lastWinTimestamp;
   }

   @Generated
   public RewardGlobalStatsModel() {
   }
}
