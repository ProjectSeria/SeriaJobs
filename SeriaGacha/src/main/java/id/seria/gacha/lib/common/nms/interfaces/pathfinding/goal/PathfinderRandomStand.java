package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import org.jetbrains.annotations.NotNull;

public final class PathfinderRandomStand extends Pathfinder {
   public PathfinderRandomStand(@NotNull FakeLivingEntity m) {
      super(m);
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[0];
   }

   @Override
   public String getInternalName() {
      return "RandomStandGoal";
   }
}
