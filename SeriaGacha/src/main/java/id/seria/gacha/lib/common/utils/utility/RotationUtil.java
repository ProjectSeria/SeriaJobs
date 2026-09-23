package id.seria.gacha.lib.common.utils.utility;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class RotationUtil {
   private RotationUtil() {
   }

   public static Vector applyYawRot(Vector vec, double degreeAngle) {
      Vector v = vec.clone();
      double sin = MathUtil.sin(Math.toRadians(degreeAngle));
      double cos = MathUtil.cos(Math.toRadians(degreeAngle));
      vec.setX(cos * v.getX() + v.getZ() * sin);
      vec.setY(v.getY());
      vec.setZ(-sin * v.getX() + v.getZ() * cos);
      return vec;
   }

   public static Vector applyPitchRot(Vector vec, double degreeAngle) {
      Vector v = vec.clone();
      double sin = MathUtil.sin(Math.toRadians(degreeAngle));
      double cos = MathUtil.cos(Math.toRadians(degreeAngle));
      vec.setX(v.getX());
      vec.setY(cos * v.getY() - v.getZ() * sin);
      vec.setZ(sin * v.getY() + v.getZ() * cos);
      return vec;
   }

   public static Vector applyRollRot(Vector vec, double degreeAngle) {
      Vector v = vec.clone();
      double sin = MathUtil.sin(Math.toRadians(degreeAngle));
      double cos = MathUtil.cos(Math.toRadians(degreeAngle));
      vec.setX(cos * v.getX() - v.getY() * sin);
      vec.setY(sin * v.getX() + v.getY() * cos);
      vec.setZ(v.getZ());
      return vec;
   }

   public static Location applyYawRot(Location loc, double degreeAngle) {
      Location l = loc.clone();
      double sin = MathUtil.sin(Math.toRadians(degreeAngle));
      double cos = MathUtil.cos(Math.toRadians(degreeAngle));
      loc.setX(cos * l.getX() + l.getZ() * sin);
      loc.setY(l.getY());
      loc.setZ(-sin * l.getX() + l.getZ() * cos);
      return loc;
   }

   public static Location applyPitchRot(Location loc, double degreeAngle) {
      Location l = loc.clone();
      double sin = MathUtil.sin(Math.toRadians(degreeAngle));
      double cos = MathUtil.cos(Math.toRadians(degreeAngle));
      loc.setX(l.getX());
      loc.setY(cos * l.getY() - l.getZ() * sin);
      loc.setZ(sin * l.getY() + l.getZ() * cos);
      return loc;
   }

   public static Location applyRollRot(Location loc, double degreeAngle) {
      Location l = loc.clone();
      double sin = MathUtil.sin(Math.toRadians(degreeAngle));
      double cos = MathUtil.cos(Math.toRadians(degreeAngle));
      loc.setX(cos * l.getX() - l.getY() * sin);
      loc.setY(sin * l.getX() + l.getY() * cos);
      loc.setZ(l.getZ());
      return loc;
   }
}
