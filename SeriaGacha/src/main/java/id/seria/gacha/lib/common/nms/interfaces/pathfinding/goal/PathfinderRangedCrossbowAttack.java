package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Ranged;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.SpeedModifier;
import org.jetbrains.annotations.NotNull;

public final class PathfinderRangedCrossbowAttack extends Pathfinder implements SpeedModifier, Ranged {
   private double speedMod;
   private float range;

   public PathfinderRangedCrossbowAttack(@NotNull FakeLivingEntity m) {
      this(m, 1.5);
   }

   public PathfinderRangedCrossbowAttack(@NotNull FakeLivingEntity m, double speedMod) {
      this(m, speedMod, 6.0F);
   }

   public PathfinderRangedCrossbowAttack(@NotNull FakeLivingEntity m, double speedMod, float range) {
      super(m);
      this.speedMod = speedMod;
      this.range = range;
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
      return "PathfinderGoalCrossbowAttack";
   }
}
