package id.seria.gacha.lib.common.nms.v26_R2.entity;

import com.google.common.collect.ImmutableList;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.enums.Pose;
import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;
import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacketsBundle;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedEntityDestroyPacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedEntityEventPacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedEntityMetadataPacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedEntitySetPassengersPacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedEntityTeleportPacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedEntityVelocityPacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedPacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedSpawnEntityPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class FakeEntityImpl implements FakeEntity {
   protected final JavaPlugin plugin;
   protected final FakeEntityType type;
   protected final Entity nmsEntity;
   private List<Player> audience;
   protected boolean beingWatched;
   protected boolean positionChanged;
   protected boolean metadataChanged;
   protected boolean passengerChanged;
   protected boolean velocityChanged;
   protected double renderDistance = 20.0;
   protected boolean dead;

   public FakeEntityImpl(JavaPlugin plugin, FakeEntityType type, Entity nmsEntity) {
      this.audience = Collections.synchronizedList(new ArrayList<>());
      this.plugin = plugin;
      this.type = type;
      this.nmsEntity = nmsEntity;
   }

   @Override
   public UUID getUniqueId() {
      return this.nmsEntity.getUUID();
   }

   @Override
   public int getId() {
      return this.nmsEntity.getId();
   }

   @Override
   public int getNetworkId() {
      return this.type.getNetworkId();
   }

   @Override
   public void addToWorld(World world, SpawnReason reason) {
      ((CraftWorld)world).getHandle().addFreshEntity(this.nmsEntity, reason);
   }

   public FakeEntityImpl setPositionAndRotation(Location location) {
      this.setPosition(location.getX(), location.getY(), location.getZ());
      this.setBodyYaw(location.getYaw());
      this.setHeadPitch(location.getPitch());
      return this;
   }

   public FakeEntityImpl setPosition(double x, double y, double z) {
      this.nmsEntity.setPos(x, y, z);
      this.positionChanged = true;
      return this;
   }

   public FakeEntityImpl setBodyYaw(float yaw) {
      this.nmsEntity.setYRot(yaw);
      this.positionChanged = true;
      return this;
   }

   public FakeEntityImpl setHeadPitch(float pitch) {
      this.nmsEntity.setXRot(pitch);
      this.positionChanged = true;
      return this;
   }

   public FakeEntityImpl setInvisible(boolean flag) {
      this.nmsEntity.setInvisible(flag);
      this.metadataChanged = true;
      return this;
   }

   public FakeEntityImpl setCustomNameVisible(boolean flag) {
      this.nmsEntity.setCustomNameVisible(flag);
      this.metadataChanged = true;
      return this;
   }

   public FakeEntityImpl setCustomName(String name) {
      this.nmsEntity.setCustomName(CraftChatMessage.fromStringOrNull(name));
      this.metadataChanged = true;
      return this;
   }

   public FakeEntityImpl setPassenger(FakeEntity other) {
      if (this.getNmsEntity().passengers.size() > 0) {
         this.getNmsEntity().passengers = ImmutableList.of();
         this.passengerChanged = true;
      }

      if (other == null) {
         return this;
      }

      Validate.isTrue(other instanceof FakeEntityImpl, "Fake Entity wrong instance, expected FakeEntityImpl");
      FakeEntityImpl casted = (FakeEntityImpl)other;
      this.getNmsEntity().passengers = ImmutableList.of(casted.getNmsEntity());
      this.passengerChanged = true;
      return this;
   }

   public FakeEntityImpl setVelocity(Vector vector) {
      this.nmsEntity.setDeltaMovement(vector.getX(), vector.getY(), vector.getZ());
      this.velocityChanged = true;
      return this;
   }

   @Override
   public FakeEntity setPose(Pose pose) {
      this.getNmsEntity().setPose(net.minecraft.world.entity.Pose.valueOf(pose.name()));
      this.metadataChanged = true;
      return this;
   }

   public FakeEntityImpl setGravity(boolean flag) {
      this.getNmsEntity().setNoGravity(!flag);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeEntity setSilent(boolean silent) {
      this.getNmsEntity().setSilent(silent);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeEntity setGlowing(boolean glowing) {
      this.getNmsEntity().setGlowingTag(glowing);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeEntity setOnGround(boolean onGround) {
      this.getNmsEntity().setOnGround(onGround);
      this.metadataChanged = true;
      this.positionChanged = true;
      return this;
   }

   @Override
   public Location getLocation() {
      return this.nmsEntity.getBukkitEntity().getLocation();
   }

   @Override
   public boolean isVehicle() {
      return this.nmsEntity.isVehicle();
   }

   public FakeArmorStandEntityImpl asArmorStand() {
      Validate.isTrue(this instanceof FakeArmorStandEntityImpl, "This entity is not an armorstand");
      return (FakeArmorStandEntityImpl)this;
   }

   public FakeLivingEntityImpl asLivingEntity() {
      Validate.isTrue(this instanceof FakeLivingEntityImpl, "This entity is not a living entity");
      return (FakeLivingEntityImpl)this;
   }

   public FakeEquipableEntityImpl asEquipableEntity() {
      Validate.isTrue(this instanceof FakeLivingEntityImpl, "This entity is not a living entity");
      return (FakeEquipableEntityImpl)this;
   }

   @Override
   public org.bukkit.entity.Entity getBukkitEntity() {
      return this.nmsEntity.getBukkitEntity();
   }

   public SynchedEntityData getDataWatcher() {
      return this.nmsEntity.getEntityData();
   }

   @Override
   public FakePacket prepareSpawnPacket() {
      return new WrappedSpawnEntityPacket(this);
   }

   @Override
   public FakePacket prepareMetadataPacket() {
      return new WrappedEntityMetadataPacket(this, true);
   }

   @Override
   public FakePacket prepareTeleportPacket() {
      FakePacketsBundle bundle = new FakePacketsBundle();
      bundle.addPackets(new WrappedEntityTeleportPacket(this), new WrappedPacket() {
         @Override
         public Packet<?> getPacket() {
            byte xRot = (byte)(FakeEntityImpl.this.getNmsEntity().getYHeadRot() * 256.0F / 360.0F);
            return new ClientboundRotateHeadPacket(FakeEntityImpl.this.getNmsEntity(), xRot);
         }
      });
      return bundle;
   }

   @Override
   public FakePacket prepareDestroyPacket() {
      return new WrappedEntityDestroyPacket(this);
   }

   @Override
   public FakePacket prepareSetPassengersPacket() {
      return new WrappedEntitySetPassengersPacket(this);
   }

   @Override
   public FakePacket prepareVelocityPacket() {
      return new WrappedEntityVelocityPacket(this);
   }

   @Override
   public FakePacket prepareEventPacket(byte event) {
      return new WrappedEntityEventPacket(this, event);
   }

   @Override
   public boolean hasEquipmentChanged() {
      return false;
   }

   @Override
   public void setEquipmentChanged(boolean value) {
   }

   @Override
   public boolean hasGravity() {
      return !this.getNmsEntity().isNoGravity();
   }

   @Override
   public boolean hasMetadataChanged() {
      return this.metadataChanged;
   }

   @Override
   public boolean hasPositionChanged() {
      return this.positionChanged;
   }

   @Override
   public boolean hasPassengerChanged() {
      return this.passengerChanged;
   }

   @Override
   public boolean hasVelocityChanged() {
      return this.velocityChanged;
   }

   @Override
   public void setAudience(List<Player> newAudience) {
      synchronized (this.audience) {
         if (this.isBeingWatched()) {
            FakePacket cachedPacket = null;

            for (Player player : newAudience) {
               if (!this.audience.contains(player)) {
                  if (cachedPacket == null) {
                     cachedPacket = this.prepareBundlePacket();
                  }

                  cachedPacket.sendPacket(player);
               }
            }

            cachedPacket = null;

            for (Player player : this.audience) {
               if (!newAudience.contains(player)) {
                  if (cachedPacket == null) {
                     cachedPacket = this.prepareDestroyPacket();
                  }

                  cachedPacket.sendPacket(player);
               }
            }
         }

         this.audience.clear();
         this.audience.addAll(newAudience);
      }
   }

   @Override
   public List<Player> getAudience() {
      synchronized (this.audience) {
         return this.audience;
      }
   }

   @Generated
   @Override
   public JavaPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   @Override
   public FakeEntityType getType() {
      return this.type;
   }

   @Generated
   public Entity getNmsEntity() {
      return this.nmsEntity;
   }

   @Generated
   @Override
   public boolean isBeingWatched() {
      return this.beingWatched;
   }

   @Generated
   @Override
   public double getRenderDistance() {
      return this.renderDistance;
   }

   @Generated
   @Override
   public boolean isDead() {
      return this.dead;
   }

   @Generated
   @Override
   public void setBeingWatched(boolean beingWatched) {
      this.beingWatched = beingWatched;
   }

   @Generated
   @Override
   public void setPositionChanged(boolean positionChanged) {
      this.positionChanged = positionChanged;
   }

   @Generated
   @Override
   public void setMetadataChanged(boolean metadataChanged) {
      this.metadataChanged = metadataChanged;
   }

   @Generated
   @Override
   public void setPassengerChanged(boolean passengerChanged) {
      this.passengerChanged = passengerChanged;
   }

   @Generated
   @Override
   public void setVelocityChanged(boolean velocityChanged) {
      this.velocityChanged = velocityChanged;
   }

   @Generated
   @Override
   public void setRenderDistance(double renderDistance) {
      this.renderDistance = renderDistance;
   }

   @Generated
   public void setDead(boolean dead) {
      this.dead = dead;
   }
}
