package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Probable;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Ranged;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.target.Filtering;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public final class PathfinderLookAtEntity<T extends LivingEntity> extends Pathfinder implements Filtering<T>, Probable, Ranged {
   public static final float DEFAULT_PROBABILITY = 0.02F;
   private float lookRange;
   private float probability;
   private Class<T> filterClass;
   private boolean horizontal;

   public PathfinderLookAtEntity(@NotNull FakeLivingEntity m, @NotNull Class<T> filter) {
      this(m, filter, 5.0F);
   }

   public PathfinderLookAtEntity(@NotNull FakeLivingEntity m, @NotNull Class<T> filter, float lookRange) {
      this(m, filter, lookRange, 0.02F);
   }

   public PathfinderLookAtEntity(@NotNull FakeLivingEntity m, @NotNull Class<T> filter, float lookRange, float probability) {
      this(m, filter, lookRange, probability, false);
   }

   public PathfinderLookAtEntity(@NotNull FakeLivingEntity m, @NotNull Class<T> filter, float lookRange, float probability, boolean horizontal) {
      super(m);
      this.filterClass = filter;
      this.lookRange = lookRange;
      this.probability = probability;
      this.horizontal = horizontal;
   }

   @Override
   public float getRange() {
      return this.lookRange;
   }

   @Override
   public void setRange(float range) {
      this.lookRange = range;
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
   public void setFilter(@NotNull Class<T> clazz) {
      this.filterClass = clazz;
   }

   @Override
   public Class<T> getFilter() {
      return this.filterClass;
   }

   public boolean isHorizontal() {
      return this.horizontal;
   }

   public void setHorizontal(boolean horizontal) {
      this.horizontal = horizontal;
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.LOOKING};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalLookAtPlayer";
   }
}
