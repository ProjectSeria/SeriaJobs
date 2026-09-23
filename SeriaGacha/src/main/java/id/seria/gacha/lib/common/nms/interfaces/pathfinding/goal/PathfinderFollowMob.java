package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Ranged;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.SpeedModifier;
import org.jetbrains.annotations.NotNull;

public final class PathfinderFollowMob extends Pathfinder implements SpeedModifier, Ranged {
   private float range;
   private double speedMod;
   private float stopDistance;

   public PathfinderFollowMob(@NotNull FakeLivingEntity m) {
      this(m, 1.0F);
   }

   public PathfinderFollowMob(@NotNull FakeLivingEntity m, float stopDistance) {
      this(m, stopDistance, 5.0F);
   }

   public PathfinderFollowMob(@NotNull FakeLivingEntity m, float stopDistance, float lookRange) {
      this(m, 1.5, stopDistance, lookRange);
   }

   public PathfinderFollowMob(@NotNull FakeLivingEntity m, double speedMod, float stopDistance, float lookRange) {
      super(m);
      this.range = lookRange;
      this.speedMod = speedMod;
      this.stopDistance = stopDistance;
   }

   public float getStopDistance() {
      return this.stopDistance;
   }

   public void setStopDistance(float stop) {
      this.stopDistance = stop;
   }

   @Override
   public float getRange() {
      return this.range;
   }

   @Override
   public void setRange(float range) {
      this.range = range;
   }

   @Override
   public double getSpeedModifier() {
      return this.speedMod;
   }

   @Override
   public void setSpeedModifier(double mod) {
      this.speedMod = mod;
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.MOVEMENT, Pathfinder.PathfinderFlag.LOOKING};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalFollowEntity";
   }
}
