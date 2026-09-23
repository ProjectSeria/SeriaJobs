package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import org.jetbrains.annotations.NotNull;

public final class PathfinderFollowBoat extends Pathfinder {
   public PathfinderFollowBoat(@NotNull FakeLivingEntity c) {
      super(c);
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[0];
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalFollowBoat";
   }
}
