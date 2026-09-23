package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.SpeedModifier;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.target.Targeting;
import org.jetbrains.annotations.NotNull;

public class PathfinderMeleeAttack extends Pathfinder implements SpeedModifier, Targeting {
   private double speedMod;
   private boolean mustSee;

   public PathfinderMeleeAttack(@NotNull FakeLivingEntity c) {
      this(c, 1.5);
   }

   public PathfinderMeleeAttack(@NotNull FakeLivingEntity c, double speedMod) {
      this(c, speedMod, true);
   }

   public PathfinderMeleeAttack(@NotNull FakeLivingEntity c, double speedMod, boolean see) throws IllegalArgumentException {
      super(c);
      this.speedMod = speedMod;
      this.mustSee = see;
   }

   @Override
   public double getSpeedModifier() {
      return this.speedMod;
   }

   @Override
   public void setSpeedModifier(double mod) {
      this.speedMod = mod;
   }

   @Override
   public boolean mustSee() {
      return this.mustSee;
   }

   @Override
   public void setSee(boolean see) {
      this.mustSee = see;
   }

   @NotNull
   @Override
   public Pathfinder.PathfinderFlag[] getFlags() {
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.MOVEMENT, Pathfinder.PathfinderFlag.LOOKING};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalMeleeAttack";
   }
}
