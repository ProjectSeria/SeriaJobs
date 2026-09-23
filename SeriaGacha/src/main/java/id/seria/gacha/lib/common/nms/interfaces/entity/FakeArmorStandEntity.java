package id.seria.gacha.lib.common.nms.interfaces.entity;

import org.bukkit.util.EulerAngle;

public interface FakeArmorStandEntity extends FakeEquipableEntity {
   FakeArmorStandEntity setHeadPose(EulerAngle var1);

   FakeArmorStandEntity setBodyPose(EulerAngle var1);

   FakeArmorStandEntity setLeftArmPose(EulerAngle var1);

   FakeArmorStandEntity setRightArmPose(EulerAngle var1);

   FakeArmorStandEntity setLeftLegPose(EulerAngle var1);

   FakeArmorStandEntity setRightLegPose(EulerAngle var1);

   EulerAngle getHeadPose();

   EulerAngle getBodyPose();

   EulerAngle getLeftArmPose();

   EulerAngle getRightArmPose();

   EulerAngle getLeftLegPose();

   EulerAngle getRightLegPose();

   FakeArmorStandEntity setSmall(boolean var1);

   FakeArmorStandEntity setArms(boolean var1);

   FakeArmorStandEntity setBasePlate(boolean var1);

   FakeArmorStandEntity setMarker(boolean var1);

   boolean isSmall();

   boolean hasArms();

   boolean hasBasePlate();

   boolean hasMarker();
}
