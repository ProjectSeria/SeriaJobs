package id.seria.gacha.api.player;

import id.seria.gacha.api.sdk.database.DbModel;

public interface PlayerDbModel extends DbModel {
   @Override
   Object getLock();

   void setLock(Object var1);

   @Override
   int getId();

   @Override
   void setId(int var1);

   String getUniqueId();

   void setUniqueId(String var1);

   String getPlayerName();

   void setPlayerName(String var1);

   String getOpenedCratesAmount();

   void setOpenedCratesAmount(String var1);

   String getOpenedPouchesAmount();

   void setOpenedPouchesAmount(String var1);

   String getCratesCooldown();

   void setCratesCooldown(String var1);

   String getRewardsWinAmount();

   void setRewardsWinAmount(String var1);

   String getRewardsCooldown();

   void setRewardsCooldown(String var1);

   String getVirtualKeys();

   void setVirtualKeys(String var1);

   String getRerolls();

   void setRerolls(String var1);

   String getItemsToClaim();

   void setItemsToClaim(String var1);
}
