package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Repeated;
import org.jetbrains.annotations.NotNull;

public final class PathfinderDolphinJump extends Pathfinder implements Repeated {
   private int interval;

   public PathfinderDolphinJump(@NotNull FakeLivingEntity d, int interval) throws IllegalArgumentException {
      super(d);
      if (interval < 0) {
         throw new IllegalArgumentException("Interval must be greater than 0");
      }

      this.interval = interval;
   }

   @Override
   public int getInterval() {
      return this.interval;
   }

   @Override
   public void setInterval(int interval) throws IllegalArgumentException {
      if (interval < 0) {
         throw new IllegalArgumentException("Must be greater than 0");
      }

      this.interval = interval;
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.MOVEMENT, Pathfinder.PathfinderFlag.JUMPING};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalWaterJump";
   }
}
