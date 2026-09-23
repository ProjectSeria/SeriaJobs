package id.seria.gacha.lib.common.utils.utility;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtil {
   public LocationUtil() {
   }

   @Deprecated
   public static Location unserializeLocation(String loc) {
      return unserialize(loc);
   }

   public static Location unserialize(String loc) {
      String[] split = loc.split(";");
      return split.length < 4
         ? null
         : new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
   }

   @Deprecated
   public static String serializeLocation(Location location) {
      return serialize(location);
   }

   public static String serialize(Location location) {
      return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
   }

   public static Location center(Location location) {
      return new Location(
         location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5, location.getYaw(), location.getPitch()
      );
   }

   public static Location center(List<Location> locations) {
      if (locations != null && !locations.isEmpty()) {
         double totalX = 0.0;
         double totalY = 0.0;
         double totalZ = 0.0;

         for (Location loc : locations) {
            totalX += loc.getX();
            totalY += loc.getY();
            totalZ += loc.getZ();
         }

         int size = locations.size();
         return new Location(locations.get(0).getWorld(), totalX / size, totalY / size, totalZ / size);
      } else {
         return null;
      }
   }
}
