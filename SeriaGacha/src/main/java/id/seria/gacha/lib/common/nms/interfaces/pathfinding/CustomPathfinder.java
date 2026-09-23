package id.seria.gacha.lib.common.nms.interfaces.pathfinding;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public abstract class CustomPathfinder extends Pathfinder {
   protected CustomPathfinder(@NotNull FakeLivingEntity m) {
      super(m);
   }

   @NotNull
   @Override
   public abstract Pathfinder.PathfinderFlag[] getFlags();

   public abstract boolean canStart();

   public abstract void start();

   public abstract void tick();

   public void stop() {
   }

   public boolean canInterrupt() {
      return true;
   }

   public boolean canContinueToUse() {
      return this.canStart();
   }

   /** @deprecated */
   @Internal
   @Override
   public String getInternalName() {
      return "CustomPathfinder";
   }
}
