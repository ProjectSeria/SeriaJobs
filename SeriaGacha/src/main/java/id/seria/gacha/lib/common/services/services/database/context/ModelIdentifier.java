package id.seria.gacha.lib.common.services.services.database.context;

import id.seria.gacha.api.sdk.database.DbModel;

public interface ModelIdentifier extends DbModel {
   @Override
   void setId(int var1);

   @Override
   int getId();

   @Override
   Object getLock();
}
