package id.seria.gacha.lib.common.nms.interfaces.pathfinding.goal;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeLivingEntity;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.SpeedModifier;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PathfinderFollowPlayer extends Pathfinder implements SpeedModifier {
   private Player player;
   private double speedMod;
   private float stopDist;
   private float startDist;
   private boolean canFly;

   public PathfinderFollowPlayer(@NotNull FakeLivingEntity t, Player player, float startDistance, float stopDistance) {
      this(t, player, 1.0, startDistance, stopDistance);
   }

   public PathfinderFollowPlayer(@NotNull FakeLivingEntity t, Player player, double speedMod, float startDistance, float stopDistance) {
      this(t, player, speedMod, startDistance, stopDistance, true);
   }

   public PathfinderFollowPlayer(@NotNull FakeLivingEntity t, Player player, double speedMod, float startDistance, float stopDistance, boolean fly) {
      super(t);
      this.player = player;
      this.speedMod = speedMod;
      this.stopDist = stopDistance;
      this.startDist = startDistance;
      this.canFly = fly;
   }

   public float getStartDistance() {
      return this.startDist;
   }

   public float getStopDistance() {
      return this.stopDist;
   }

   public void setStartDistance(float start) {
      this.startDist = start;
   }

   public void setStopDistance(float stop) {
      this.stopDist = stop;
   }

   public boolean canFly() {
      return this.canFly;
   }

   public void setCanFly(boolean fly) {
      this.canFly = fly;
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
      return new Pathfinder.PathfinderFlag[]{Pathfinder.PathfinderFlag.MOVEMENT, Pathfinder.PathfinderFlag.LOOKING};
   }

   @Override
   public String getInternalName() {
      return "PathfinderGoalFollowPlayer";
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
