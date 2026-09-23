package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.SpeedModifier;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.target.Filtering;
import java.util.function.Predicate;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PathfinderAvoidEntity<T extends LivingEntity> extends Pathfinder implements Filtering<T>, SpeedModifier {
   private double speedModifier;
   private float maxDistance;
   private double sprintModifier;
   private Class<T> filter;
   private Predicate<T> avoidPredicate;
   private Predicate<T> avoidingPredicate;

   public PathfinderAvoidEntity(@NotNull FakeLivingEntity m, @NotNull Class<T> filter) throws IllegalArgumentException {
      this(m, filter, 5.0F, 1.5);
   }

   public PathfinderAvoidEntity(@NotNull FakeLivingEntity m, @NotNull Class<T> filter, float dist, double mod) throws IllegalArgumentException {
      this(m, filter, dist, mod, mod);
   }

   public PathfinderAvoidEntity(@NotNull FakeLivingEntity m, @NotNull Class<T> filter, float dist, double walkMod, double sprintMod) throws IllegalArgumentException {
      this(m, filter, dist, walkMod, sprintMod, null);
   }

   public PathfinderAvoidEntity(
      @NotNull FakeLivingEntity m, @NotNull Class<T> filter, float dist, double walkMod, double sprintMod, @Nullable Predicate<T> avoidPredicate
   ) throws IllegalArgumentException {
      this(m, filter, dist, walkMod, sprintMod, avoidPredicate, null);
   }

   public PathfinderAvoidEntity(
      @NotNull FakeLivingEntity m,
      @NotNull Class<T> filter,
      float dist,
      double walkMod,
      double sprintMod,
      @Nullable Predicate<T> avoidPredicate,
      @Nullable Predicate<T> avoidingPredicate
   ) throws IllegalArgumentException {
      super(m);
      this.filter = filter;
      this.speedModifier = walkMod;
      this.maxDistance = dist;
      this.sprintModifier = sprintMod;
      this.avoidPredicate = avoidPredicate;
      this.avoidingPredicate = avoidingPredicate;
   }

   public float getMaxDistance() {
      return this.maxDistance;
   }

   public void setMaxDistance(float dist) {
      this.maxDistance = dist;
   }

   @Override
   public double getSpeedModifier() {
      return this.speedModifier;
   }

   public double getSprintModifier() {
      return this.sprintModifier;
   }

   public void setSprintModifier(double mod) {
      this.sprintModifier = mod;
   }

   @Nullable
   public Predicate<T> getAvoidPredicate() {
      return this.avoidPredicate;
   }

   public void setAvoidPredicate(@Nullable Predicate<T> avoidPredicate) {
      this.avoidPredicate = avoidPredicate;
   }

   @Nullable
   public Predicate<T> getAvoidingPredicate() {
      return this.avoidingPredicate;
   }

   public void setAvoidingPredicate(@Nullable Predicate<T> avoidingPredicate) {
      this.avoidingPredicate = avoidingPredicate;
   }

   @Override
   public void setSpeedModifier(double mod) {
      this.speedModifier = mod;
   }

   @Override
   public void setFilter(@NotNull Class<T> clazz) {
      this.filter = clazz;
   }

   @NotNull
   @Override
   public Class<T> getFilter() {
      return this.filter;
   }

   @NotNull
   @Override
   public FakeLivingEntity getEntity() {
      return this.entity;
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.MOVEMENT};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalAvoidTarget";
   }
}
