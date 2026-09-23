package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Repeated;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.SpeedModifier;
import org.jetbrains.annotations.NotNull;

public class PathfinderRandomStroll extends Pathfinder implements SpeedModifier, Repeated {
   private double speedMod;
   private int interval;

   public PathfinderRandomStroll(@NotNull FakeLivingEntity c) {
      this(c, 1.0);
   }

   public PathfinderRandomStroll(@NotNull FakeLivingEntity c, double speedMod) {
      this(c, speedMod, 120);
   }

   public PathfinderRandomStroll(@NotNull FakeLivingEntity c, double speedMod, int interval) throws IllegalArgumentException {
      super(c);
      if (interval < 0) {
         throw new IllegalArgumentException("Interval must be greater than 0");
      }

      this.speedMod = speedMod;
      this.interval = interval;
   }

   @Override
   public int getInterval() {
      return this.interval;
   }

   @Override
   public void setInterval(int interval) {
      if (interval < 0) {
         throw new IllegalArgumentException("Interval must be greater than 0");
      }

      this.interval = interval;
   }

   @Override
   public double getSpeedModifier() {
      return this.speedMod;
   }

   @Override
   public void setSpeedModifier(double mod) {
      this.speedMod = mod;
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.MOVEMENT};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalRandomStroll";
   }
}
