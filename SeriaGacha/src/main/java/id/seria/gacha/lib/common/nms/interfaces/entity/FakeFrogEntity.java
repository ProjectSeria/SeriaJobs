package id.seria.gacha.lib.common.nms.interfaces.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.enums.FrogVariant;

public interface FakeFrogEntity extends FakeLivingEntity {
   FakeFrogEntity setVariant(FrogVariant var1);

   FrogVariant getVariant();
}
