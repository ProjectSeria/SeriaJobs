package id.seria.gacha.lib.common.utils.utility.quaternion;

import org.bukkit.util.Vector;

public class MathUtil {
   private static final int CHUNK_BITS = 4;
   private static final int CHUNK_VALUES = 16;
   public static final float DEGTORAD = (float) (Math.PI / 180.0);
   public static final float RADTODEG = (float) (180.0 / Math.PI);
   public static final double HALFROOTOFTWO = 0.707106781;

   public MathUtil() {
   }

   public static double lengthSquared(double... values) {
      double rval = 0.0;

      for (double value : values) {
         rval += value * value;
      }

      return rval;
   }

   public static double length(double... values) {
      return Math.sqrt(lengthSquared(values));
   }

   public static double distance(double x1, double y1, double x2, double y2) {
      return length(x1 - x2, y1 - y2);
   }

   public static double distanceSquared(double x1, double y1, double x2, double y2) {
      return lengthSquared(x1 - x2, y1 - y2);
   }

   public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
      return length(x1 - x2, y1 - y2, z1 - z2);
   }

   public static double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
      return lengthSquared(x1 - x2, y1 - y2, z1 - z2);
   }

   public static double getPercentage(int subtotal, int total, int decimals) {
      return round(getPercentage(subtotal, total), decimals);
   }

   public static double getPercentage(int subtotal, int total) {
      return (float)subtotal / total * 100.0F;
   }

   public static int getAngleDifference(int angle1, int angle2) {
      return Math.abs(wrapAngle(angle1 - angle2));
   }

   public static float getAngleDifference(float angle1, float angle2) {
      return Math.abs(wrapAngle(angle1 - angle2));
   }

   public static double getAngleDifference(double angle1, double angle2) {
      return Math.abs(wrapAngle(angle1 - angle2));
   }

   public static int wrapAngle(int angle) {
      int wrappedAngle = angle;

      while (wrappedAngle <= -180) {
         wrappedAngle += 360;
      }

      while (wrappedAngle > 180) {
         wrappedAngle -= 360;
      }

      return wrappedAngle;
   }

   public static float wrapAngle(float angle) {
      float wrappedAngle = angle;

      while (wrappedAngle <= -180.0F) {
         wrappedAngle += 360.0F;
      }

      while (wrappedAngle > 180.0F) {
         wrappedAngle -= 360.0F;
      }

      return wrappedAngle;
   }

   public static double wrapAngle(double angle) {
      double wrappedAngle = angle;

      while (wrappedAngle <= -180.0) {
         wrappedAngle += 360.0;
      }

      while (wrappedAngle > 180.0) {
         wrappedAngle -= 360.0;
      }

      return wrappedAngle;
   }

   public static double normalize(double x, double z, double reqx, double reqz) {
      return Math.sqrt(lengthSquared(reqx, reqz) / lengthSquared(x, z));
   }

   public static float getLookAtYaw(Vector motion) {
      return getLookAtYaw(motion.getX(), motion.getZ());
   }

   public static float getLookAtYaw(double dx, double dz) {
      return atan2(dz, dx) - 180.0F;
   }

   public static float getLookAtPitch(double dX, double dY, double dZ) {
      return getLookAtPitch(dY, length(dX, dZ));
   }

   public static float getLookAtPitch(double dY, double dXZ) {
      return -atan(dY / dXZ);
   }

   public static float atan(double value) {
      return (float)Math.toDegrees(CommonTrigMath.atan(value));
   }

   public static float atan2(double y, double x) {
      return (float)Math.toDegrees(CommonTrigMath.atan2(y, x));
   }

   public static long longFloor(double value) {
      long l = (long)value;
      return value < l ? l - 1L : l;
   }

   public static int floor(double value) {
      int i = (int)value;
      return value < i ? i - 1 : i;
   }

   public static int floor(float value) {
      int i = (int)value;
      return value < i ? i - 1 : i;
   }

   public static int ceil(double value) {
      int i = (int)value;
      return value > i ? i + 1 : i;
   }

   public static int ceil(float value) {
      int i = (int)value;
      return value > i ? i + 1 : i;
   }

   public static Vector rotate(float yaw, float pitch, Vector vector) {
      return rotate(yaw, pitch, vector.getX(), vector.getY(), vector.getZ());
   }

   public static Vector rotate(float yaw, float pitch, double x, double y, double z) {
      double angle = Math.toRadians(yaw);
      double sinyaw = Math.sin(angle);
      double cosyaw = Math.cos(angle);
      angle = Math.toRadians(pitch);
      double sinpitch = Math.sin(angle);
      double cospitch = Math.cos(angle);
      Vector vector = new Vector();
      vector.setX(x * sinyaw - y * cosyaw * sinpitch - z * cosyaw * cospitch);
      vector.setY(y * cospitch - z * sinpitch);
      vector.setZ(-(x * cosyaw) - y * sinyaw * sinpitch - z * sinyaw * cospitch);
      return vector;
   }

   public static int floorMod(int x, int y) {
      return Math.floorMod(x, y);
   }

   public static long floorMod(long x, long y) {
      return Math.floorMod(x, y);
   }

   public static int floorDiv(int x, int y) {
      return Math.floorDiv(x, y);
   }

   public static long floorDiv(long x, long y) {
      return Math.floorDiv(x, y);
   }

   public static double round(double value, int decimals) {
      double p = Math.pow(10.0, decimals);
      return Math.round(value * p) / p;
   }

   public static double fixNaN(double value) {
      return fixNaN(value, 0.0);
   }

   public static float fixNaN(float value) {
      return fixNaN(value, 0.0F);
   }

   public static double fixNaN(double value, double def) {
      return Double.isNaN(value) ? def : value;
   }

   public static float fixNaN(float value, float def) {
      return Float.isNaN(value) ? def : value;
   }

   public static int toChunk(double loc) {
      return floor(loc / 16.0);
   }

   public static int toChunk(int loc) {
      return loc >> 4;
   }

   public static double useOld(double oldvalue, double newvalue, double peruseold) {
      return oldvalue + peruseold * (newvalue - oldvalue);
   }

   public static double lerp(double d1, double d2, double stage) {
      if (Double.isNaN(stage) || stage > 1.0) {
         return d2;
      } else {
         return stage < 0.0 ? d1 : d1 * (1.0 - stage) + d2 * stage;
      }
   }

   public static Vector lerp(Vector vec1, Vector vec2, double stage) {
      Vector newvec = new Vector();
      newvec.setX(lerp(vec1.getX(), vec2.getX(), stage));
      newvec.setY(lerp(vec1.getY(), vec2.getY(), stage));
      newvec.setZ(lerp(vec1.getZ(), vec2.getZ(), stage));
      return newvec;
   }

   public static boolean isInverted(double value1, double value2) {
      return value1 > 0.0 && value2 < 0.0 || value1 < 0.0 && value2 > 0.0;
   }

   public static Vector getDirection(float yaw, float pitch) {
      Vector vector = new Vector();
      double rotX = Math.toRadians(yaw);
      double rotY = Math.toRadians(pitch);
      vector.setY(-Math.sin(rotY));
      double h = Math.cos(rotY);
      vector.setX(-h * Math.sin(rotX));
      vector.setZ(h * Math.cos(rotX));
      return vector;
   }

   public static double clamp(double value, double limit) {
      return clamp(value, -limit, limit);
   }

   public static double clamp(double value, double min, double max) {
      return value < min ? min : (value > max ? max : value);
   }

   public static float clamp(float value, float limit) {
      return clamp(value, -limit, limit);
   }

   public static float clamp(float value, float min, float max) {
      return value < min ? min : (value > max ? max : value);
   }

   public static int clamp(int value, int limit) {
      return clamp(value, -limit, limit);
   }

   public static int clamp(int value, int min, int max) {
      return value < min ? min : (value > max ? max : value);
   }

   public static long clamp(long value, long limit) {
      return clamp(value, -limit, limit);
   }

   public static long clamp(long value, long min, long max) {
      return value < min ? min : (value > max ? max : value);
   }

   public static int invert(int value, boolean negative) {
      return negative ? -value : value;
   }

   public static float invert(float value, boolean negative) {
      return negative ? -value : value;
   }

   public static double invert(double value, boolean negative) {
      return negative ? -value : value;
   }

   public static long toLong(int msw, int lsw) {
      return ((long)msw << 32) + lsw - -2147483648L;
   }

   public static long longHashToLong(int msw, int lsw) {
      return ((long)msw << 32) + lsw - -2147483648L;
   }

   public static int longHashMsw(long key) {
      return (int)(key >> 32);
   }

   public static int longHashLsw(long key) {
      return (int)(key & -1L) + Integer.MIN_VALUE;
   }

   public static long longHashSumW(long keyA, long keyB) {
      long sum_msw = (keyA & -4294967296L) + (keyB & -4294967296L);
      long sum_lsw = (keyA & -1L) + (keyB & -1L);
      return sum_msw + (int)sum_lsw - -2147483648L;
   }

   public static long longHashAdd(long key_a, long key_b) {
      return key_a + key_b + -2147483648L;
   }

   public static void setVectorLength(Vector vector, double length) {
      setVectorLengthSquared(vector, Math.signum(length) * length * length);
   }

   public static void setVectorLengthSquared(Vector vector, double lengthsquared) {
      double vlength = vector.lengthSquared();
      if (Math.abs(vlength) > 1.0E-4) {
         if (lengthsquared < 0.0) {
            vector.multiply(-Math.sqrt(-lengthsquared / vlength));
         } else {
            vector.multiply(Math.sqrt(lengthsquared / vlength));
         }
      }
   }

   public static boolean isHeadingTo(Vector offset, Vector velocity) {
      double dbefore = offset.lengthSquared();
      if (dbefore < 1.0E-4) {
         return true;
      }

      Vector clonedVelocity = velocity.clone();
      setVectorLengthSquared(clonedVelocity, dbefore);
      return dbefore > clonedVelocity.subtract(offset).lengthSquared();
   }

   public static double getNormalizationFactor(Vector v) {
      return getNormalizationFactorLS(v.lengthSquared());
   }

   public static double getNormalizationFactor(double x, double y, double z, double w) {
      return getNormalizationFactorLS(x * x + y * y + z * z + w * w);
   }

   public static double getNormalizationFactor(double x, double y, double z) {
      return getNormalizationFactorLS(x * x + y * y + z * z);
   }

   public static double getNormalizationFactor(double x, double y) {
      return getNormalizationFactorLS(x * x + y * y);
   }

   public static double getNormalizationFactorLS(double lengthSquared) {
      return Math.abs(1.0 - lengthSquared) < 2.107342E-8 ? 2.0 / (1.0 + lengthSquared) : 1.0 / Math.sqrt(lengthSquared);
   }

   public static double getAngleDifference(Vector v0, Vector v1) {
      double dot = v0.dot(v1);
      dot *= getNormalizationFactor(v0);
      dot *= getNormalizationFactor(v1);
      return Math.toDegrees(Math.acos(dot));
   }

   public static Vector setVector(Vector vector, Vector value) {
      return vector.copy(value);
   }

   public static Vector setVector(Vector vector, double x, double y, double z) {
      vector.setX(x);
      vector.setY(y);
      vector.setZ(z);
      return vector;
   }

   public static Vector addToVector(Vector vector, double ax, double ay, double az) {
      vector.setX(vector.getX() + ax);
      vector.setY(vector.getY() + ay);
      vector.setZ(vector.getZ() + az);
      return vector;
   }

   public static Vector subtractFromVector(Vector vector, double sx, double sy, double sz) {
      vector.setX(vector.getX() - sx);
      vector.setY(vector.getY() - sy);
      vector.setZ(vector.getZ() - sz);
      return vector;
   }

   public static Vector multiplyVector(Vector vector, double mx, double my, double mz) {
      vector.setX(vector.getX() * mx);
      vector.setY(vector.getY() * my);
      vector.setZ(vector.getZ() * mz);
      return vector;
   }

   public static Vector divideVector(Vector vector, double mx, double my, double mz) {
      vector.setX(vector.getX() / mx);
      vector.setY(vector.getY() / my);
      vector.setZ(vector.getZ() / mz);
      return vector;
   }
}
