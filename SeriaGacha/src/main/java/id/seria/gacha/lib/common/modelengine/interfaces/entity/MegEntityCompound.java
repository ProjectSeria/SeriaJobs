package id.seria.gacha.lib.common.modelengine.interfaces.entity;

import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;

public interface MegEntityCompound {
   MegActiveModel getActiveModel();

   MegModeledEntity getModeledEntity();

   void release();
}
