package id.seria.gacha.lib.common.nms.v26_R2.pathfinding;

import id.seria.gacha.lib.common.nms.interfaces.pathfinding.CustomPathfinder;
import net.minecraft.world.entity.ai.goal.Goal;

public final class CustomPathFindingImpl extends Goal {
   private final CustomPathfinder p;

   public CustomPathFindingImpl(CustomPathfinder p) {
      this.p = p;
   }

   public boolean canUse() {
      return this.p.canStart();
   }

   public boolean canContinueToUse() {
      return this.p.canContinueToUse();
   }

   public boolean isInterruptable() {
      return this.p.canInterrupt();
   }

   public void start() {
      this.p.start();
   }

   public void tick() {
      this.p.tick();
   }

   public void stop() {
      this.p.stop();
   }

   public CustomPathfinder getPathfinder() {
      return this.p;
   }
}
