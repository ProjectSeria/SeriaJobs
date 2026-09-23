package id.seria.gacha.lib.common.utils.utility;

import id.seria.gacha.lib.common.nms.interfaces.MathAdapter;
import id.seria.gacha.lib.common.utils.version.ClassNMSLoader;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class MathUtil {
   private static final Random random = new Random();
   private static MathAdapter adapter = ClassNMSLoader.load(MathAdapter.class);

   public MathUtil() {
   }

   public static int max(Integer... numbers) {
      int max = 0;

      for (Integer i : numbers) {
         max = Math.max(max, i);
      }

      return max;
   }

   public static double max(Double... numbers) {
      double max = 0.0;

      for (Double i : numbers) {
         max = Math.max(max, i);
      }

      return max;
   }

   public static int min(Integer... numbers) {
      int min = 0;

      for (Integer i : numbers) {
         min = Math.min(min, i);
      }

      return min;
   }

   public static double min(Double... numbers) {
      double min = 0.0;

      for (Double i : numbers) {
         min = Math.min(min, i);
      }

      return min;
   }

   public static <T> T random(List<T> list) {
      return list.size() == 0 ? null : list.get(random(0, list.size() - 1));
   }

   public static <T> T random(T[] array) {
      return array.length == 0 ? null : array[random(0, array.length - 1)];
   }

   public static int random(int min, int max) {
      return random.nextInt(max + 1 - min) + min;
   }

   public static double random(double min, double max) {
      return min + random.nextDouble() * (max - min);
   }

   public static float random(float min, float max) {
      return min + random.nextFloat() * (max - min);
   }

   public static double lerp(double d1, double d2, double stage) {
      if (Double.isNaN(stage) || stage > 1.0) {
         return d2;
      } else {
         return stage < 0.0 ? d1 : d1 * (1.0 - stage) + d2 * stage;
      }
   }

   public static float lerp(float d1, float d2, float stage) {
      if (Double.isNaN(stage) || stage > 1.0F) {
         return d2;
      } else {
         return stage < 0.0F ? d1 : d1 * (1.0F - stage) + d2 * stage;
      }
   }

   public static Vector lerp(Vector vec1, Vector vec2, double stage) {
      Vector newvec = new Vector();
      newvec.setX(lerp(vec1.getX(), vec2.getX(), stage));
      newvec.setY(lerp(vec1.getY(), vec2.getY(), stage));
      newvec.setZ(lerp(vec1.getZ(), vec2.getZ(), stage));
      return newvec;
   }

   public static Location lerp(Location loc1, Location loc2, double stage) {
      Location newloc = new Location(loc1.getWorld(), 0.0, 0.0, 0.0);
      newloc.setX(lerp(loc1.getX(), loc2.getX(), stage));
      newloc.setY(lerp(loc1.getY(), loc2.getY(), stage));
      newloc.setZ(lerp(loc1.getZ(), loc2.getZ(), stage));
      newloc.setYaw((float)lerp(loc1.getYaw(), loc2.getYaw(), stage));
      newloc.setPitch((float)lerp(loc1.getPitch(), loc2.getPitch(), stage));
      return newloc;
   }

   public static int randomSign() {
      return random(0, 1) == 0 ? -1 : 1;
   }

   public static double clamp(double val, double min, double max) {
      return Math.max(min, Math.min(max, val));
   }

   public static double clamp(float val, float min, float max) {
      return Math.max(min, Math.min(max, val));
   }

   public static int clamp(int val, int min, int max) {
      return Math.max(min, Math.min(max, val));
   }

   public static double sin(double radians) {
      return adapter.sin(radians);
   }

   public static double cos(double radians) {
      return adapter.cos(radians);
   }

   public static float sin(float radians) {
      return (float)adapter.sin(radians);
   }

   public static float cos(float radians) {
      return (float)adapter.cos(radians);
   }
}
