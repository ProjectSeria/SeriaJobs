package id.seria.gacha.lib.common.nms.interfaces.pathfinding;

import org.jetbrains.annotations.Nullable;

public final class WrappedPathfinder {
   private final int priority;
   private final Pathfinder pathfinder;

   public WrappedPathfinder(@Nullable Pathfinder p, int priority) throws IllegalArgumentException {
      if (priority < 0) {
         throw new IllegalArgumentException("Priority must be greater than 0");
      }

      this.pathfinder = p;
      this.priority = priority;
   }

   public int getPriority() {
      return this.priority;
   }

   @Nullable
   public Pathfinder getPathfinder() {
      return this.pathfinder;
   }
}
