package id.seria.gacha.lib.common.utils.utility;

import org.bukkit.block.BlockFace;

public class FaceUtil {
   public static final BlockFace[] AXIS = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
   public static final BlockFace[] RADIAL = new BlockFace[]{
      BlockFace.NORTH,
      BlockFace.NORTH_NORTH_EAST,
      BlockFace.NORTH_EAST,
      BlockFace.EAST_NORTH_EAST,
      BlockFace.EAST,
      BlockFace.EAST_SOUTH_EAST,
      BlockFace.SOUTH_EAST,
      BlockFace.SOUTH_SOUTH_EAST,
      BlockFace.SOUTH,
      BlockFace.SOUTH_SOUTH_WEST,
      BlockFace.SOUTH_WEST,
      BlockFace.WEST_SOUTH_WEST,
      BlockFace.WEST,
      BlockFace.WEST_NORTH_WEST,
      BlockFace.NORTH_WEST,
      BlockFace.NORTH_NORTH_WEST
   };

   public FaceUtil() {
   }

   public static BlockFace yawToFace(float yaw) {
      return yawToFace(yaw, true);
   }

   public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
      return useSubCardinalDirections ? RADIAL[Math.floorMod(Math.round(yaw / 22.5F), 16)] : AXIS[Math.floorMod(Math.round(yaw / 90.0F), 4)];
   }
}
