package id.seria.gacha.lib.common.nms.interfaces.particles.utils;

import java.util.Random;

public final class MathUtils {
   public static final Random RANDOM = new Random();

   public MathUtils() {
   }

   public static int generateRandomInteger(int minimum, int maximum) {
      return minimum + (int)(RANDOM.nextDouble() * (maximum - minimum + 1));
   }

   public static int getMaxOrMin(int value, int max, int min) {
      return value < max ? Math.max(value, min) : max;
   }
}
