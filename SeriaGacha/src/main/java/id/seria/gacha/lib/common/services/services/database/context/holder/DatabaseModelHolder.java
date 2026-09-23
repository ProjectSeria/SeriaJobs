package id.seria.gacha.lib.common.services.services.database.context.holder;

import id.seria.gacha.lib.common.services.services.database.context.ModelIdentifier;

public interface DatabaseModelHolder<T extends ModelIdentifier> {
   int getDatabaseId();

   void setDatabaseId(int var1);

   T getDatabaseModel();

   void markDirty();

   void unmarkDirty();

   boolean isDirty();
}
