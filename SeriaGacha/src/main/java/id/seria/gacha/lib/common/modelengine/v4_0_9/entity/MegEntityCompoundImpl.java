package id.seria.gacha.lib.common.modelengine.v4_0_9.entity;

import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegEntityCompound;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegModeledEntity;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import java.util.Objects;
import lombok.Generated;

public class MegEntityCompoundImpl implements MegEntityCompound {
   private MegActiveModel activeModel;
   private MegModeledEntity modeledEntity;

   public MegEntityCompoundImpl(MegActiveModel activeModel, MegModeledEntity modeledEntity) {
      this.activeModel = Objects.requireNonNull(activeModel);
      this.modeledEntity = Objects.requireNonNull(modeledEntity);
   }

   @Override
   public void release() {
   }

   @Generated
   @Override
   public MegActiveModel getActiveModel() {
      return this.activeModel;
   }

   @Generated
   @Override
   public MegModeledEntity getModeledEntity() {
      return this.modeledEntity;
   }
}
