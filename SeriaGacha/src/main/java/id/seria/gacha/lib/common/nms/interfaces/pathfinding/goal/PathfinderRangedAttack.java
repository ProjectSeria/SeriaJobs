package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Ranged;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.SpeedModifier;
import org.jetbrains.annotations.NotNull;

public final class PathfinderRangedAttack extends Pathfinder implements SpeedModifier, Ranged {
   private double speedMod;
   private float range;
   private int aMin;
   private int aMax;

   public PathfinderRangedAttack(@NotNull FakeLivingEntity m, double speedMod) {
      this(m, speedMod, 6.0F);
   }

   public PathfinderRangedAttack(@NotNull FakeLivingEntity m, double speedMod, float range) {
      this(m, speedMod, range, 100);
   }

   public PathfinderRangedAttack(@NotNull FakeLivingEntity m, double speedMod, float range, int interval) {
      this(m, speedMod, range, interval, interval);
   }

   public PathfinderRangedAttack(@NotNull FakeLivingEntity m, double speedMod, float range, int attackMin, int attackMax) {
      super(m);
      this.speedMod = speedMod;
      this.range = range;
      this.aMax = attackMax;
      this.aMin = attackMin;
   }

   public int getMinAttackInterval() {
      return this.aMin;
   }

   public int getMaxAttackInterval() {
      return this.aMax;
   }

   public void setMinAttackInterval(int min) {
      this.aMin = min;
   }

   public void setMaxAttackInterval(int max) {
      this.aMax = max;
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
      return "PathfinderGoalArrowAttack";
   }
}
