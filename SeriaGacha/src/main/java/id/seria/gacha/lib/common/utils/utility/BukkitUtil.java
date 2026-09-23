package id.seria.gacha.lib.common.utils.utility;

import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.version.VersionUtils;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class BukkitUtil {
   public BukkitUtil() {
   }

   public static void runSync(ServerPlugin plugin, Runnable runnable) {
      if (!VersionUtils.isFoliaServer() && Bukkit.isPrimaryThread()) {
         runnable.run();
      } else if (plugin.isEnabled()) {
         plugin.getScheduler().runSync(runnable);
      } else {
         plugin.getLogger().info("BukkitUtil#runSync runnable ignored when disabling.");
      }
   }

   public static void runSync(ServerPlugin plugin, Location location, Runnable runnable) {
      if (VersionUtils.isFoliaServer()) {
         plugin.getScheduler().runSync(location, runnable);
      } else {
         if (Bukkit.isPrimaryThread()) {
            runnable.run();
         } else if (plugin.isEnabled()) {
            plugin.getScheduler().runSync(runnable);
         } else {
            plugin.getLogger().info("BukkitUtil#runSync runnable ignored when disabling.");
         }
      }
   }

   public static boolean teleportPlayer(Player player, Location location) {
      try {
         List<Entity> passengers = player.getPassengers();
         if (!passengers.isEmpty()) {
            for (Entity passenger : passengers) {
               passenger.leaveVehicle();
            }
         }
      } catch (NoSuchMethodError var5) {
      }

      return player.teleport(location);
   }

   public static EulerAngle createEulerAngleFromRad(float xRad, float yRad, float zRad) {
      return new EulerAngle(Math.toDegrees(xRad), Math.toDegrees(yRad), Math.toDegrees(zRad));
   }

   public static EulerAngle createEulerAngleFromRad(double xRad, double yRad, double zRad) {
      return new EulerAngle(Math.toDegrees(xRad), Math.toDegrees(yRad), Math.toDegrees(zRad));
   }

   public static BlockFace calculateBlockFace(float yaw) {
      BlockFace[] axis = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
      return axis[Math.round(yaw / 90.0F) & 3];
   }

   public static String translateColorCodes(String rawMessage) {
      return rawMessage == null ? "" : ChatColor.translateAlternateColorCodes('&', rawMessage);
   }

   @Deprecated
   public static String extractName(ItemStack itemStack) {
      if (itemStack == null) {
         return "\u00a77Empty";
      } else {
         return itemStack.getType() != Material.AIR && itemStack.getItemMeta() != null
            ? "\u00a77" + ItemBuilder.of(itemStack).getDisplayName()
            : StringUtil.humanize(itemStack.getType());
      }
   }

   public static double calculateYaw(BlockFace face) {
      switch (face) {
         case SOUTH:
            return 180.0;
         case WEST:
            return 90.0;
         case NORTH:
            return 0.0;
         case EAST:
            return -90.0;
         default:
            return 0.0;
      }
   }

   public static List<Player> getNearbyPlayers(Location location, double radius) {
      org.apache.commons.lang.Validate.isTrue(radius > 0.0, "Radius need to be higher than 0");
      double squareRadius = radius * radius;
      List<Player> players = new ArrayList<>();
      World world = location.getWorld();

      for (Player player : Bukkit.getOnlinePlayers()) {
         Location playerLocation = player.getLocation();
         if (world.equals(playerLocation.getWorld()) && playerLocation.distanceSquared(location) <= squareRadius) {
            players.add(player);
         }
      }

      return players;
   }

   public static boolean hasDecimal(Vector vector) {
      return vector.getX() % 1.0 != 0.0 || vector.getY() % 1.0 != 0.0 || vector.getZ() % 1.0 != 0.0;
   }

   public static boolean hasDecimal(Location vector) {
      return vector.getX() % 1.0 != 0.0 || vector.getY() % 1.0 != 0.0 || vector.getZ() % 1.0 != 0.0;
   }
}
