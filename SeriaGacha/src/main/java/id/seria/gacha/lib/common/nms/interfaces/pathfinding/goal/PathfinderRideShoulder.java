package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import org.jetbrains.annotations.NotNull;

public final class PathfinderRideShoulder extends Pathfinder {
   public PathfinderRideShoulder(@NotNull FakeLivingEntity p) {
      super(p);
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[0];
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalPerch";
   }
}
