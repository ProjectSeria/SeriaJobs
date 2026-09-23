package id.seria.gacha.lib.common.nms.interfaces.entity;

public interface FakeInteractionEntity extends FakeEntity {
   float getInteractionWidth();

   FakeInteractionEntity setInteractionWidth(float var1);

   float getInteractionHeight();

   FakeInteractionEntity setInteractionHeight(float var1);

   boolean isResponsive();

   FakeInteractionEntity setResponsive(boolean var1);
}
