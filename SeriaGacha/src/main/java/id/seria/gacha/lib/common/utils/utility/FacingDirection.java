package id.seria.gacha.lib.common.utils.utility;

import id.seria.gacha.lib.xseries.XMaterial;
import lombok.Generated;
import org.bukkit.block.BlockFace;

public enum FacingDirection {
   NORTH(BlockFace.NORTH),
   EAST(BlockFace.EAST),
   SOUTH(BlockFace.SOUTH),
   WEST(BlockFace.WEST),
   NORTH_EAST(BlockFace.NORTH_EAST),
   NORTH_WEST(BlockFace.NORTH_WEST),
   SOUTH_EAST(BlockFace.SOUTH_EAST),
   SOUTH_WEST(BlockFace.SOUTH_WEST),
   WEST_NORTH_WEST(BlockFace.WEST_NORTH_WEST),
   NORTH_NORTH_WEST(BlockFace.NORTH_NORTH_WEST),
   NORTH_NORTH_EAST(BlockFace.NORTH_NORTH_EAST),
   EAST_NORTH_EAST(BlockFace.EAST_NORTH_EAST),
   EAST_SOUTH_EAST(BlockFace.EAST_SOUTH_EAST),
   SOUTH_SOUTH_EAST(BlockFace.SOUTH_SOUTH_EAST),
   SOUTH_SOUTH_WEST(BlockFace.SOUTH_SOUTH_WEST),
   WEST_SOUTH_WEST(BlockFace.WEST_SOUTH_WEST),
   UNKNOWN(BlockFace.SELF);

   private BlockFace rawBukkit;
   private double direction;

   FacingDirection(BlockFace rawBukkit) {
      this.rawBukkit = rawBukkit;
      this.direction = Math.toDegrees(Math.atan2(-rawBukkit.getModX(), rawBukkit.getModZ()));
   }

   public BlockFace toBukkit(XMaterial material) {
      if (this.rawBukkit.equals(BlockFace.SELF)) {
         return BlockFace.SOUTH;
      }

      if (material.name().contains("SHULKER")) {
         return BlockFace.UP;
      }

      if (!material.name().contains("SKULL") && !material.name().contains("HEAD")) {
         switch (this) {
            case NORTH:
            case EAST:
            case SOUTH:
            case WEST:
               return this.rawBukkit;
            default:
               return FaceUtil.yawToFace((float)this.direction, false);
         }
      } else {
         return this.rawBukkit;
      }
   }

   public static FacingDirection fromBukkit(BlockFace bukkit) {
      switch (bukkit) {
         case UP:
         case DOWN:
         case SELF:
            return UNKNOWN;
         default:
            return valueOf(bukkit.name());
      }
   }

   @Generated
   public BlockFace getRawBukkit() {
      return this.rawBukkit;
   }

   @Generated
   public double getDirection() {
      return this.direction;
   }
}
