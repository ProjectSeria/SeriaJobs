package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Probable;
import org.jetbrains.annotations.NotNull;

public final class PathfinderRandomStrollLand extends PathfinderRandomStroll implements Probable {
   public static final float DEFAULT_PROBABILITY = 0.001F;
   private float probability;

   public PathfinderRandomStrollLand(@NotNull FakeLivingEntity c) {
      this(c, 1.0);
   }

   public PathfinderRandomStrollLand(@NotNull FakeLivingEntity c, double speedMod) {
      this(c, speedMod, 0.001F);
   }

   public PathfinderRandomStrollLand(@NotNull FakeLivingEntity c, double speedMod, float probability) {
      super(c, speedMod);
      this.probability = probability;
   }

   @Override
   public float getProbability() {
      return this.probability;
   }

   @Override
   public void setProbability(float prob) {
      this.probability = prob;
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalRandomStrollLand";
   }
}
