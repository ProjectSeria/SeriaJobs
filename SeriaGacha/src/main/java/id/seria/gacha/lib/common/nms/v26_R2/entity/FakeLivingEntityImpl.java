package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.attributes.Attribute;
import id.seria.gacha.lib.common.nms.interfaces.entity.attributes.AttributeType;
import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;
import id.seria.gacha.lib.common.nms.v26_R2.entity.attributes.AttributeImpl;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedEntityAttributesPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeLivingEntityImpl extends FakeEntityImpl implements FakeLivingEntity {
   public FakeLivingEntityImpl(JavaPlugin plugin, FakeEntityType type, LivingEntity nmsEntity) {
      super(plugin, type, nmsEntity);
   }

   @Override
   public FakeEntityImpl setPositionAndRotation(Location location) {
      super.setPositionAndRotation(location);
      this.setHeadYaw(location.getYaw());
      return this;
   }

   public FakeLivingEntityImpl setHeadYaw(float yaw) {
      this.getNmsEntity().setYHeadRot(yaw);
      this.positionChanged = true;
      return this;
   }

   @Override
   public Attribute getAttribute(AttributeType type) {
      org.bukkit.attribute.Attribute bukkitType = this.getBukkitAttribute(type);
      AttributeInstance instance = null;
      if (bukkitType != null) {
         instance = this.getBukkitEntity().getAttribute(bukkitType);
      }

      return new AttributeImpl(instance);
   }

   public org.bukkit.attribute.Attribute getBukkitAttribute(AttributeType type) {
      switch (type) {
         case MAX_HEALTH:
            return org.bukkit.attribute.Attribute.MAX_HEALTH;
         case FOLLOW_RANGE:
            return org.bukkit.attribute.Attribute.FOLLOW_RANGE;
         case KNOCKBACK_RESISTANCE:
            return org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE;
         case MOVEMENT_SPEED:
            return org.bukkit.attribute.Attribute.MOVEMENT_SPEED;
         case FLYING_SPEED:
            return org.bukkit.attribute.Attribute.FLYING_SPEED;
         case ATTACK_DAMAGE:
            return org.bukkit.attribute.Attribute.ATTACK_DAMAGE;
         case ATTACK_KNOCKBACK:
            return org.bukkit.attribute.Attribute.ATTACK_KNOCKBACK;
         case ATTACK_SPEED:
            return org.bukkit.attribute.Attribute.ATTACK_SPEED;
         case ARMOR:
            return org.bukkit.attribute.Attribute.ARMOR;
         case ARMOR_TOUGHNESS:
            return org.bukkit.attribute.Attribute.ARMOR_TOUGHNESS;
         case LUCK:
            return org.bukkit.attribute.Attribute.LUCK;
         case MAX_ABSORPTION:
            return org.bukkit.attribute.Attribute.MAX_ABSORPTION;
         case JUMP_STRENGTH:
            return org.bukkit.attribute.Attribute.JUMP_STRENGTH;
         case ZOMBIE_SPAWN_REINFORCEMENTS:
            return org.bukkit.attribute.Attribute.SPAWN_REINFORCEMENTS;
         default:
            return null;
      }
   }

   @Override
   public FakeLivingEntity setAI(boolean ai) {
      if (this.getNmsEntity() instanceof Mob) {
         ((Mob)this.getNmsEntity()).setNoAi(!ai);
      }

      return this;
   }

   @Override
   public org.bukkit.entity.LivingEntity getBukkitEntity() {
      return (org.bukkit.entity.LivingEntity)super.getBukkitEntity();
   }

   public LivingEntity getNmsEntity() {
      return (LivingEntity)this.nmsEntity;
   }

   @Override
   public FakePacket prepareAttributesPacket() {
      return new WrappedEntityAttributesPacket(this);
   }
}
