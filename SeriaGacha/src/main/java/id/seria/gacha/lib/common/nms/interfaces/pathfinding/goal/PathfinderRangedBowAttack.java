package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Ranged;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Repeated;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.SpeedModifier;
import org.jetbrains.annotations.NotNull;

public final class PathfinderRangedBowAttack extends Pathfinder implements SpeedModifier, Ranged, Repeated {
   private double speedMod;
   private float range;
   private int aInv;

   public PathfinderRangedBowAttack(@NotNull FakeLivingEntity m, double speedMod) {
      this(m, speedMod, 6.0F);
   }

   public PathfinderRangedBowAttack(@NotNull FakeLivingEntity m, double speedMod, float range) {
      this(m, speedMod, range, 100);
   }

   public PathfinderRangedBowAttack(@NotNull FakeLivingEntity m, double speedMod, float range, int interval) throws IllegalArgumentException {
      super(m);
      if (interval < 0) {
         throw new IllegalArgumentException("Interval must be greater than 0");
      }

      this.speedMod = speedMod;
      this.range = range;
      this.aInv = interval;
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

   @Override
   public int getInterval() {
      return this.aInv;
   }

   @Override
   public void setInterval(int interval) {
      if (interval < 0) {
         throw new IllegalArgumentException("Interval must be greater than 0");
      }

      this.aInv = interval;
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.LOOKING, Pathfinder.PathfinderFlag.MOVEMENT};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalBowShoot";
   }
}
