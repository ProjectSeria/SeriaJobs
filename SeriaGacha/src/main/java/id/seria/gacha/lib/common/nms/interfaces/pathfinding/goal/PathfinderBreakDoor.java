package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Conditional;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import java.util.Arrays;
import java.util.function.Predicate;
import org.bukkit.Difficulty;
import org.jetbrains.annotations.NotNull;

public final class PathfinderBreakDoor extends Pathfinder implements Conditional<Difficulty> {
   public static final int DEFAULT_DOOR_BREAK_TIME = 240;
   private int breakTime;
   private Predicate<Difficulty> validDiffs;

   public PathfinderBreakDoor(@NotNull FakeLivingEntity m) {
      this(m, 240, Difficulty.NORMAL);
   }

   public PathfinderBreakDoor(@NotNull FakeLivingEntity m, Difficulty... validDifficulties) {
      this(m, 240, validDifficulties);
   }

   public PathfinderBreakDoor(@NotNull FakeLivingEntity m, int breakTime, Difficulty... validDifficulties) throws IllegalArgumentException {
      this(m, breakTime, d -> Arrays.asList(validDifficulties).contains(d));
   }

   public PathfinderBreakDoor(@NotNull FakeLivingEntity m, int breakTime, Predicate<Difficulty> validDiffs) throws IllegalArgumentException {
      super(m);
      if (breakTime <= 0) {
         throw new IllegalArgumentException("Break Time must be greater than 0");
      }

      this.breakTime = breakTime;
      this.validDiffs = validDiffs;
   }

   public int getBreakTime() {
      return this.breakTime;
   }

   @NotNull
   @Override
   public Predicate<Difficulty> getCondition() {
      return this.validDiffs;
   }

   public void setBreakTime(int time) throws IllegalArgumentException {
      if (time <= 0) {
         throw new IllegalArgumentException("Break Time must be greater than 0");
      }

      this.breakTime = time;
   }

   @Override
   public void setCondition(@NotNull Predicate<Difficulty> condition) {
      this.validDiffs = condition;
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[0];
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalBreakDoor";
   }
}
