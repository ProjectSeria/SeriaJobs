package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.SpeedModifier;
import org.jetbrains.annotations.NotNull;

public final class PathfinderPanic extends Pathfinder implements SpeedModifier {
   private double speedMod;

   public PathfinderPanic(@NotNull FakeLivingEntity m) {
      this(m, 1.5);
   }

   public PathfinderPanic(@NotNull FakeLivingEntity m, double speedMod) {
      super(m);
      this.speedMod = speedMod;
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
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.MOVEMENT};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalPanic";
   }
}
