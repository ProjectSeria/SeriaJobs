package id.seria.gacha.lib.common.nms.v26_R2.pathfinding;

import java.util.EnumSet;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTeleportEvent;

public class PathfinderGoalFollowPlayerImpl extends Goal {
   public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
   private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
   private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
   private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
   private final Mob tamable;
   private LivingEntity owner;
   private final LevelReader level;
   private final double speedModifier;
   private final PathNavigation navigation;
   private int timeToRecalcPath;
   private final float stopDistance;
   private final float startDistance;
   private float oldWaterCost;
   private final boolean canFly;

   public PathfinderGoalFollowPlayerImpl(Mob tamable, Player owner, double d0, float f, float f1, boolean flag) {
      this.tamable = tamable;
      this.owner = owner;
      this.level = tamable.level();
      this.speedModifier = d0;
      this.navigation = tamable.getNavigation();
      this.startDistance = f;
      this.stopDistance = f1;
      this.canFly = flag;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      if (!(tamable.getNavigation() instanceof GroundPathNavigation) && !(tamable.getNavigation() instanceof FlyingPathNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
      }
   }

   public boolean canUse() {
      if (this.owner == null) {
         return false;
      } else if (this.owner.isSpectator()) {
         return false;
      } else {
         return this.unableToMove() ? false : !(this.tamable.distanceToSqr(this.owner) < this.startDistance * this.startDistance);
      }
   }

   public boolean canContinueToUse() {
      return this.navigation.isDone() ? false : (this.unableToMove() ? false : this.tamable.distanceToSqr(this.owner) > this.stopDistance * this.stopDistance);
   }

   private boolean unableToMove() {
      return this.tamable.isPassenger() || this.tamable.isLeashed();
   }

   public void start() {
      this.timeToRecalcPath = 0;
      this.oldWaterCost = this.tamable.getPathfindingMalus(PathType.WATER);
      this.tamable.setPathfindingMalus(PathType.WATER, 0.0F);
   }

   public void stop() {
      this.navigation.stop();
      this.tamable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
   }

   public void tick() {
      boolean var0 = this.shouldTryTeleportToOwner();
      if (!var0) {
         this.tamable.getLookControl().setLookAt(this.owner, 10.0F, this.tamable.getMaxHeadXRot());
      }

      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = this.adjustedTickDelay(10);
         if (var0) {
            this.tryToTeleportToOwner();
         } else {
            this.navigation.moveTo(this.owner, this.speedModifier);
         }
      }
   }

   public boolean shouldTryTeleportToOwner() {
      LivingEntity entityliving = this.getOwner();
      return entityliving != null && this.tamable.distanceToSqr(this.getOwner()) >= 144.0;
   }

   public void tryToTeleportToOwner() {
      LivingEntity entityliving = this.getOwner();
      if (entityliving != null) {
         this.teleportToAroundBlockPos(entityliving.blockPosition());
      }
   }

   private void teleportToAroundBlockPos(BlockPos blockposition) {
      for (int i = 0; i < 10; i++) {
         int j = this.tamable.getRandom().nextIntBetweenInclusive(-3, 3);
         int k = this.tamable.getRandom().nextIntBetweenInclusive(-3, 3);
         if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
            int l = this.tamable.getRandom().nextIntBetweenInclusive(-1, 1);
            if (this.maybeTeleportTo(blockposition.getX() + j, blockposition.getY() + l, blockposition.getZ() + k)) {
               return;
            }
         }
      }
   }

   private boolean maybeTeleportTo(int i, int j, int k) {
      if (!this.canTeleportTo(new BlockPos(i, j, k))) {
         return false;
      }

      EntityTeleportEvent event = CraftEventFactory.callEntityTeleportEvent(this.tamable, i + 0.5, j, k + 0.5);
      if (event.isCancelled()) {
         return false;
      }

      Location to = event.getTo();
      this.tamable.moveTo(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
      this.navigation.stop();
      return true;
   }

   public boolean canTeleportTo(BlockPos blockposition) {
      PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(this.tamable, blockposition);
      if (pathtype != PathType.WALKABLE) {
         return false;
      }

      BlockState iblockdata = this.tamable.level().getBlockState(blockposition.below());
      BlockPos blockposition1 = blockposition.subtract(this.tamable.blockPosition());
      return this.tamable.level().noCollision(this.tamable, this.tamable.getBoundingBox().move(blockposition1));
   }

   @Generated
   public Mob getTamable() {
      return this.tamable;
   }

   @Generated
   public LivingEntity getOwner() {
      return this.owner;
   }

   @Generated
   public LevelReader getLevel() {
      return this.level;
   }

   @Generated
   public double getSpeedModifier() {
      return this.speedModifier;
   }

   @Generated
   public PathNavigation getNavigation() {
      return this.navigation;
   }

   @Generated
   public int getTimeToRecalcPath() {
      return this.timeToRecalcPath;
   }

   @Generated
   public float getStopDistance() {
      return this.stopDistance;
   }

   @Generated
   public float getStartDistance() {
      return this.startDistance;
   }

   @Generated
   public float getOldWaterCost() {
      return this.oldWaterCost;
   }

   @Generated
   public boolean isCanFly() {
      return this.canFly;
   }
}
