package id.seria.gacha.lib.common.nms.interfaces.pathfinding;

import org.jetbrains.annotations.ApiStatus.Internal;

public interface PathfinderInfo {
   default String getName() {
      return this.getClass().getSimpleName();
   }

   @Internal
   String getInternalName();
}
