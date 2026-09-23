package id.seria.gacha.lib.common.nms.interfaces.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.enums.Pose;
import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;
import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacketsBundle;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public interface FakeEntity {
   FakeEntityType getType();

   UUID getUniqueId();

   int getId();

   int getNetworkId();

   void addToWorld(World var1, SpawnReason var2);

   FakeEntity setPositionAndRotation(Location var1);

   FakeEntity setPosition(double var1, double var3, double var5);

   FakeEntity setBodyYaw(float var1);

   FakeEntity setHeadPitch(float var1);

   FakeEntity setInvisible(boolean var1);

   FakeEntity setCustomNameVisible(boolean var1);

   FakeEntity setCustomName(String var1);

   FakeEntity setPassenger(FakeEntity var1);

   FakeEntity setVelocity(Vector var1);

   FakeEntity setPose(Pose var1);

   FakeEntity setGravity(boolean var1);

   default FakeEntity setSilent(boolean silent) {
      throw new NotImplementedException("Still not implemented on this version");
   }

   default FakeEntity setGlowing(boolean glowing) {
      throw new NotImplementedException("Still not implemented on this version");
   }

   default FakeEntity tick() {
      throw new NotImplementedException("Still not implemented on this version");
   }

   default FakeEntity setOnGround(boolean onGround) {
      throw new NotImplementedException("Still not implemented on this version");
   }

   boolean hasGravity();

   Location getLocation();

   boolean isVehicle();

   default FakeArmorStandEntity asArmorStand() {
      Validate.isTrue(this instanceof FakeArmorStandEntity, "This entity is not an armorstand");
      return (FakeArmorStandEntity)this;
   }

   default FakeFrogEntity asFrog() {
      Validate.isTrue(this instanceof FakeFrogEntity, "This entity is not a frog");
      return (FakeFrogEntity)this;
   }

   default FakeLivingEntity asLivingEntity() {
      Validate.isTrue(this instanceof FakeLivingEntity, "This entity is not a living entity");
      return (FakeLivingEntity)this;
   }

   default FakeEquipableEntity asEquipableEntity() {
      Validate.isTrue(this instanceof FakeEquipableEntity, "This entity is not a equipable entity");
      return (FakeEquipableEntity)this;
   }

   FakePacket prepareSpawnPacket();

   FakePacket prepareMetadataPacket();

   FakePacket prepareTeleportPacket();

   FakePacket prepareDestroyPacket();

   FakePacket prepareSetPassengersPacket();

   FakePacket prepareVelocityPacket();

   FakePacket prepareEventPacket(byte var1);

   List<Player> getAudience();

   void setAudience(List<Player> var1);

   void setMetadataChanged(boolean var1);

   void setEquipmentChanged(boolean var1);

   void setPositionChanged(boolean var1);

   void setPassengerChanged(boolean var1);

   void setVelocityChanged(boolean var1);

   boolean hasMetadataChanged();

   boolean hasEquipmentChanged();

   boolean hasPositionChanged();

   boolean hasPassengerChanged();

   boolean hasVelocityChanged();

   boolean isBeingWatched();

   void setBeingWatched(boolean var1);

   void setRenderDistance(double var1);

   double getRenderDistance();

   boolean isDead();

   JavaPlugin getPlugin();

   Entity getBukkitEntity();

   default World getWorld() {
      return this.getBukkitEntity().getWorld();
   }

   default FakePacketsBundle prepareBundlePacket() {
      FakePacketsBundle bundle = new FakePacketsBundle();
      bundle.addPacket(this.prepareSpawnPacket());
      bundle.addPacket(this.prepareMetadataPacket());
      if (this instanceof FakeEquipableEntity) {
         bundle.addPacket(((FakeEquipableEntity)this).prepareEquipmentPacket());
      }

      if (this instanceof FakeLivingEntity) {
         bundle.addPacket(((FakeLivingEntity)this).prepareAttributesPacket());
      }

      if (this.isVehicle()) {
         bundle.addPacket(this.prepareSetPassengersPacket());
      }

      bundle.addPacket(this.prepareVelocityPacket());
      return bundle;
   }

   default void setAudience(Player... newAudience) {
      this.setAudience(Arrays.asList(newAudience));
   }
}
