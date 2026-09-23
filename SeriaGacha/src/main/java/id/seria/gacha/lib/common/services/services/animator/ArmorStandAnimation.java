package id.seria.gacha.lib.common.services.services.animator;

import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class ArmorStandAnimation {
   private String name;
   private AnimationType type;
   private int durationTicks;
   private Map<String, AnimationEntity> entities = new HashMap<>();
   private Map<String, AnimationEntity> boneToEntityLookupMap = new HashMap<>();
   private Map<Integer, TimelineFrame> timeline = new HashMap<>();

   public ArmorStandAnimation(String name, AnimationType type, int durationTicks) {
      this.name = name;
      this.type = type;
      this.durationTicks = durationTicks;
   }

   public void addBone(String entityName, AnimationBone bone) {
      AnimationEntity entity = this.entities.get(entityName);
      if (entity == null) {
         entity = new AnimationEntity(entityName);
         this.entities.put(entityName, entity);
      }

      entity.getBones().put(bone.getName(), bone);
      this.boneToEntityLookupMap.put(bone.getName(), entity);
   }

   public void addEntity(AnimationEntity entity) {
      this.entities.put(entity.getName(), entity);
      this.addBone(entity.getName(), new AnimationBone(entity.getName(), BoneFlag.ENTITY));
   }

   public void addEffectFrame(int frame, String instruction) {
      TimelineFrame frameData = this.timeline.computeIfAbsent(frame, k -> new TimelineFrame());
      frameData.addScriptFrame(instruction);
   }

   public void addPositionFrame(int frame, String boneName, double x, double y, double z) {
      TimelineFrame frameData = this.timeline.computeIfAbsent(frame, k -> new TimelineFrame());
      frameData.addPositionFrame(boneName, new Vector(x, y, z));
   }

   public void addRotationFrame(int frame, String boneName, double x, double y, double z) {
      TimelineFrame frameData = this.timeline.computeIfAbsent(frame, k -> new TimelineFrame());
      frameData.addRotationFrame(boneName, new EulerAngle(x, y, z));
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public AnimationType getType() {
      return this.type;
   }

   @Generated
   public int getDurationTicks() {
      return this.durationTicks;
   }

   @Generated
   public Map<String, AnimationEntity> getEntities() {
      return this.entities;
   }

   @Generated
   public Map<String, AnimationEntity> getBoneToEntityLookupMap() {
      return this.boneToEntityLookupMap;
   }

   @Generated
   public Map<Integer, TimelineFrame> getTimeline() {
      return this.timeline;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setType(AnimationType type) {
      this.type = type;
   }

   @Generated
   public void setDurationTicks(int durationTicks) {
      this.durationTicks = durationTicks;
   }

   @Generated
   public void setEntities(Map<String, AnimationEntity> entities) {
      this.entities = entities;
   }

   @Generated
   public void setBoneToEntityLookupMap(Map<String, AnimationEntity> boneToEntityLookupMap) {
      this.boneToEntityLookupMap = boneToEntityLookupMap;
   }

   @Generated
   public void setTimeline(Map<Integer, TimelineFrame> timeline) {
      this.timeline = timeline;
   }

   @Generated
   public ArmorStandAnimation() {
   }
}
