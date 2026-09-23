package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Ranged;
import org.jetbrains.annotations.NotNull;

public final class PathfinderBeg extends Pathfinder implements Ranged {
   private float lookRange;

   public PathfinderBeg(@NotNull FakeLivingEntity w) {
      this(w, 5.0F);
   }

   public PathfinderBeg(@NotNull FakeLivingEntity w, float lookRange) {
      super(w);
      this.lookRange = lookRange;
   }

   @NotNull
   @Override
   public FakeLivingEntity getEntity() {
      return this.entity;
   }

   @Override
   public float getRange() {
      return this.lookRange;
   }

   @Override
   public void setRange(float range) {
      this.lookRange = range;
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.LOOKING};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalBeg";
   }
}
