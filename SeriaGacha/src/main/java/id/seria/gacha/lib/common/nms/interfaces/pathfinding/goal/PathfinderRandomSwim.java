package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import org.jetbrains.annotations.NotNull;

public final class PathfinderRandomSwim extends PathfinderRandomStroll {
   public static final int DEFAULT_INTERVAL = 120;

   public PathfinderRandomSwim(@NotNull FakeLivingEntity c) {
      this(c, 120);
   }

   public PathfinderRandomSwim(@NotNull FakeLivingEntity c, int interval) {
      super(c, 1.0, interval);
   }

   public PathfinderRandomSwim(@NotNull FakeLivingEntity c, double speedMod) {
      this(c, speedMod, 120);
   }

   public PathfinderRandomSwim(@NotNull FakeLivingEntity c, double speedMod, int interval) {
      super(c, speedMod, interval);
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalRandomSwim";
   }
}
