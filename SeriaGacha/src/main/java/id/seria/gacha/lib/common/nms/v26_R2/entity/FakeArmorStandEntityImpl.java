package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;

public class FakeArmorStandEntityImpl extends FakeEquipableEntityImpl implements FakeArmorStandEntity {
   public FakeArmorStandEntityImpl(JavaPlugin plugin, ArmorStand nmsEntity) {
      super(plugin, FakeEntityType.ARMOR_STAND, nmsEntity);
   }

   public FakeArmorStandEntityImpl setHeadPose(EulerAngle angle) {
      this.getNmsEntity().setHeadPose(this.toNMS(angle));
      this.metadataChanged = true;
      return this;
   }

   public FakeArmorStandEntityImpl setBodyPose(EulerAngle angle) {
      this.getNmsEntity().setBodyPose(this.toNMS(angle));
      this.metadataChanged = true;
      return this;
   }

   public FakeArmorStandEntityImpl setLeftArmPose(EulerAngle angle) {
      this.getNmsEntity().setLeftArmPose(this.toNMS(angle));
      this.metadataChanged = true;
      return this;
   }

   public FakeArmorStandEntityImpl setRightArmPose(EulerAngle angle) {
      this.getNmsEntity().setRightArmPose(this.toNMS(angle));
      this.metadataChanged = true;
      return this;
   }

   public FakeArmorStandEntityImpl setLeftLegPose(EulerAngle angle) {
      this.getNmsEntity().setLeftLegPose(this.toNMS(angle));
      this.metadataChanged = true;
      return this;
   }

   public FakeArmorStandEntityImpl setRightLegPose(EulerAngle angle) {
      this.getNmsEntity().setRightLegPose(this.toNMS(angle));
      this.metadataChanged = true;
      return this;
   }

   private Rotations toNMS(EulerAngle angle) {
      return new Rotations((float)angle.getX(), (float)angle.getY(), (float)angle.getZ());
   }

   @Override
   public EulerAngle getHeadPose() {
      return this.toBukkit(this.getNmsEntity().getHeadPose());
   }

   @Override
   public EulerAngle getBodyPose() {
      return this.toBukkit(this.getNmsEntity().getBodyPose());
   }

   @Override
   public EulerAngle getLeftArmPose() {
      return this.toBukkit(this.getNmsEntity().getLeftArmPose());
   }

   @Override
   public EulerAngle getRightArmPose() {
      return this.toBukkit(this.getNmsEntity().getRightArmPose());
   }

   @Override
   public EulerAngle getLeftLegPose() {
      return this.toBukkit(this.getNmsEntity().getLeftLegPose());
   }

   @Override
   public EulerAngle getRightLegPose() {
      return this.toBukkit(this.getNmsEntity().getRightLegPose());
   }

   private EulerAngle toBukkit(Rotations vector) {
      return new EulerAngle(vector.getX(), vector.getY(), vector.getZ());
   }

   @Override
   public FakeArmorStandEntity setSmall(boolean flag) {
      this.getNmsEntity().setSmall(flag);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeArmorStandEntity setArms(boolean flag) {
      this.getNmsEntity().setShowArms(flag);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeArmorStandEntity setBasePlate(boolean flag) {
      this.getNmsEntity().setNoBasePlate(!flag);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeArmorStandEntity setMarker(boolean flag) {
      this.getNmsEntity().setMarker(flag);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public boolean isSmall() {
      return this.getNmsEntity().isSmall();
   }

   @Override
   public boolean hasArms() {
      return this.getNmsEntity().showArms();
   }

   @Override
   public boolean hasBasePlate() {
      return !this.getNmsEntity().showBasePlate();
   }

   @Override
   public boolean hasMarker() {
      return this.getNmsEntity().isMarker();
   }

   public ArmorStand getNmsEntity() {
      return (ArmorStand)this.nmsEntity;
   }
}
