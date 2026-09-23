package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import org.jetbrains.annotations.NotNull;

public final class PathfinderOpenDoor extends Pathfinder {
   private boolean close;

   public PathfinderOpenDoor(@NotNull FakeLivingEntity m) {
      this(m, true);
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[0];
   }

   public PathfinderOpenDoor(@NotNull FakeLivingEntity m, boolean close) {
      super(m);
      this.close = close;
   }

   public boolean mustClose() {
      return this.close;
   }

   public void setMustClose(boolean close) {
      this.close = close;
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalDoorOpen";
   }
}
