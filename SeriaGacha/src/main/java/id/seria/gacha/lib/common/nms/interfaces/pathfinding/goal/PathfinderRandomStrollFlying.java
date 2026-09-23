package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import org.jetbrains.annotations.NotNull;

public final class PathfinderRandomStrollFlying extends PathfinderRandomStroll {
   public PathfinderRandomStrollFlying(@NotNull FakeLivingEntity c) {
      super(c);
   }

   public PathfinderRandomStrollFlying(@NotNull FakeLivingEntity c, double speedMod) {
      super(c, speedMod);
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalRandomFly";
   }
}
