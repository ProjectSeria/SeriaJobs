package id.seria.gacha.lib.common.nms.interfaces.pathfinding;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import org.jetbrains.annotations.NotNull;

public abstract class Pathfinder implements PathfinderInfo {
   protected final FakeLivingEntity entity;

   protected Pathfinder(@NotNull FakeLivingEntity entity) throws IllegalArgumentException {
      if (entity == null) {
         throw new IllegalArgumentException("Entity cannot be null");
      }

      this.entity = entity;
   }

   @NotNull
   public FakeLivingEntity getEntity() {
      return this.entity;
   }

   @NotNull
   public abstract Pathfinder.PathfinderFlag[] getFlags();

   public enum PathfinderFlag {
      MOVEMENT,
      TARGETING,
      LOOKING,
      JUMPING;

      PathfinderFlag() {
      }
   }
}
