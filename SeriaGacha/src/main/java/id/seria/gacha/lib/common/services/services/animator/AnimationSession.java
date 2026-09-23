package id.seria.gacha.lib.common.services.services.animator;

import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.RotationUtil;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class AnimationSession {
   private final ServerPlugin plugin;
   private final Location location;
   private final ArmorStandAnimation animation;
   private final Map<String, FakeArmorStandEntity> entities = new HashMap<>();
   private final List<Player> viewers;
   private int tick;
   private boolean started;
   private Task currentTask;
   private AnimationSession.ScriptConsumer scriptConsumer = script -> {};

   public AnimationSession(ServerPlugin plugin, Location location, ArmorStandAnimation animation, List<Player> viewers) {
      this.plugin = plugin;
      this.location = location;
      this.animation = animation;
      this.viewers = viewers;

      for (String entityName : animation.getEntities().keySet()) {
         this.entities.put(entityName, this.createEntity());
      }
   }

   private FakeArmorStandEntity createEntity() {
      FakeArmorStandEntity entity = Utilities.getNMSFactory().createFakeArmorStand(this.plugin, this.location);
      entity.setBasePlate(false);
      entity.setGravity(false);
      entity.setInvisible(false);
      entity.setArms(true);
      entity.setAudience(this.viewers);
      EulerAngle zero = new EulerAngle(0.0, 0.0, 0.0);
      entity.setHeadPose(zero);
      entity.setBodyPose(zero);
      entity.setLeftArmPose(zero);
      entity.setRightArmPose(zero);
      entity.setLeftLegPose(zero);
      entity.setRightLegPose(zero);
      return entity;
   }

   public void start() {
      if (!this.started) {
         this.started = true;
         this.tick = 0;
         this.tick();
         this.currentTask = this.plugin.getScheduler().scheduleSync(this::tick, 0L, 1L);

         for (FakeArmorStandEntity entity : this.entities.values()) {
            Services.getWatcherService().startWatching(entity, false);
         }
      }
   }

   public void stop() {
      if (this.started) {
         this.started = false;

         for (FakeArmorStandEntity entity : this.entities.values()) {
            Services.getWatcherService().stopWatching(entity);
         }

         if (this.currentTask != null) {
            this.currentTask.cancel();
         }
      }
   }

   private void tick() {
      if (this.tick > this.animation.getDurationTicks()) {
         switch (this.animation.getType()) {
            case LOOP:
               this.tick = 0;
               break;
            case HOLD:
               return;
            case ONCE:
               this.stop();
               return;
         }
      }

      TimelineFrame frame = this.animation.getTimeline().get(this.tick);
      if (frame != null) {
         for (String boneName : frame.getRotationFrames().keySet()) {
            AnimationEntity animationEntity = this.animation.getBoneToEntityLookupMap().get(boneName);
            if (animationEntity == null) {
               this.plugin.getLogger().info("Unknown bone " + boneName + " for a crate animation");
            } else {
               AnimationBone bone = animationEntity.getBones().get(boneName);
               if (bone != null) {
                  BoneFlag boneFlag = bone.getFlag();
                  EulerAngle eulerAngle = frame.getRotationFrames().get(boneName);
                  FakeArmorStandEntity entity = this.entities.get(animationEntity.getName());
                  switch (boneFlag) {
                     case ENTITY:
                        entity.setBodyYaw((float)(this.location.getYaw() + eulerAngle.getY()));
                        break;
                     case HEAD:
                        entity.setHeadPose(eulerAngle);
                        break;
                     case BODY:
                        entity.setBodyPose(eulerAngle);
                        break;
                     case LEFT_ARM:
                        entity.setLeftArmPose(eulerAngle);
                        break;
                     case RIGHT_ARM:
                        entity.setRightArmPose(eulerAngle);
                        break;
                     case LEFT_LEG:
                        entity.setLeftLegPose(eulerAngle);
                        break;
                     case RIGHT_LEG:
                        entity.setRightLegPose(eulerAngle);
                  }
               }
            }
         }

         for (String boneName : frame.getPositionFrames().keySet()) {
            AnimationEntity animationEntity = this.animation.getBoneToEntityLookupMap().get(boneName);
            if (animationEntity == null) {
               this.plugin.getLogger().info("Unknown bone " + boneName + " for a crate animation");
            } else {
               AnimationBone bone = animationEntity.getBones().get(boneName);
               if (bone != null && bone.getFlag() == BoneFlag.ENTITY) {
                  Vector position = frame.getPositionFrames().get(boneName).clone();
                  RotationUtil.applyYawRot(position, -180.0F + this.location.getYaw());
                  Location newLocation = this.location.clone().add(position.getX(), position.getY(), -position.getZ());
                  FakeArmorStandEntity entity = this.entities.get(animationEntity.getName());
                  entity.setPosition(newLocation.getX(), newLocation.getY(), newLocation.getZ());
               }
            }
         }

         for (String script : frame.getEffectFrames()) {
            for (String instruction : script.split(";")) {
               this.scriptConsumer.consume(instruction);
            }
         }
      }

      for (FakeArmorStandEntity entity : this.entities.values()) {
         if (entity.hasPositionChanged()) {
            entity.prepareTeleportPacket().sendPacket(entity.getAudience());
            entity.setPositionChanged(false);
         }

         if (entity.hasMetadataChanged()) {
            entity.prepareMetadataPacket().sendPacket(entity.getAudience());
            entity.setMetadataChanged(false);
         }
      }

      this.tick++;
   }

   public FakeArmorStandEntity getEntity(String name) {
      return this.entities.get(name);
   }

   @Deprecated
   public FakeArmorStandEntity getEntity() {
      return this.entities.values().iterator().next();
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public ArmorStandAnimation getAnimation() {
      return this.animation;
   }

   @Generated
   public Map<String, FakeArmorStandEntity> getEntities() {
      return this.entities;
   }

   @Generated
   public List<Player> getViewers() {
      return this.viewers;
   }

   @Generated
   public int getTick() {
      return this.tick;
   }

   @Generated
   public boolean isStarted() {
      return this.started;
   }

   @Generated
   public Task getCurrentTask() {
      return this.currentTask;
   }

   @Generated
   public AnimationSession.ScriptConsumer getScriptConsumer() {
      return this.scriptConsumer;
   }

   @Generated
   public void setScriptConsumer(AnimationSession.ScriptConsumer scriptConsumer) {
      this.scriptConsumer = scriptConsumer;
   }

   public interface ScriptConsumer {
      void consume(String var1);
   }
}
