package id.seria.gacha.lib.common.utils.utility;

import java.util.concurrent.TimeUnit;

public class TimeUtil {
   public TimeUtil() {
   }

   public static String formatSecondsLong(long secondsIn) {
      long dayCount = TimeUnit.SECONDS.toDays(secondsIn);
      long secondsCount = secondsIn - TimeUnit.DAYS.toSeconds(dayCount);
      long hourCount = TimeUnit.SECONDS.toHours(secondsCount);
      secondsCount -= TimeUnit.HOURS.toSeconds(hourCount);
      long minutesCount = TimeUnit.SECONDS.toMinutes(secondsCount);
      secondsCount -= TimeUnit.MINUTES.toSeconds(minutesCount);
      StringBuilder sb = new StringBuilder();
      if (dayCount != 0L) {
         sb.append(String.format("%d %s, ", dayCount, dayCount == 1L ? "day" : "days"));
      }

      if (hourCount != 0L) {
         sb.append(String.format("%d %s, ", hourCount, hourCount == 1L ? "hour" : "hours"));
      }

      if (minutesCount != 0L) {
         sb.append(String.format("%d %s ", minutesCount, minutesCount == 1L ? "minute" : "minutes"));
      }

      if (secondsCount != 0L) {
         sb.append(String.format("%d %s", secondsCount, secondsCount == 1L ? "second" : "seconds"));
      }

      if (secondsIn == 0L) {
         sb.append(String.format("%s", "0 seconds"));
      }

      return sb.toString();
   }

   public static String formatSecondsShort(int timeInSeconds) {
      int secondsLeft = timeInSeconds % 3600 % 60;
      int minutes = (int)Math.floor(timeInSeconds % 3600 / 60);
      int hours = (int)Math.floor(timeInSeconds / 3600);
      if (minutes == 0 && hours == 0) {
         return secondsLeft + "s";
      } else {
         return hours == 0 ? minutes + "m " + secondsLeft + "s" : hours + "h " + minutes + "m " + secondsLeft + "s";
      }
   }

   public static String formatLargestUnit(long millis) {
      if (millis <= 0L) {
         return "0 seconds";
      }

      long days = TimeUnit.MILLISECONDS.toDays(millis);
      if (days > 0L) {
         return days + (days == 1L ? " day" : " days");
      }

      long hours = TimeUnit.MILLISECONDS.toHours(millis);
      if (hours > 0L) {
         return hours + (hours == 1L ? " hour" : " hours");
      }

      long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
      if (minutes > 0L) {
         return minutes + (minutes == 1L ? " minute" : " minutes");
      }

      long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
      return seconds + (seconds == 1L ? " second" : " seconds");
   }

   public static String formatLargestUnit(long millis, String daysFormat, String hoursFormat, String minutesFormat, String secondsFormat) {
      if (millis <= 0L) {
         return secondsFormat.replace("%value%", "0");
      }

      long remaining = TimeUnit.MILLISECONDS.toSeconds(millis);
      long days = remaining / 86400L;
      remaining %= 86400L;
      long hours = remaining / 3600L;
      remaining %= 3600L;
      long minutes = remaining / 60L;
      long seconds = remaining % 60L;
      StringBuilder builder = new StringBuilder();
      if (days > 0L) {
         builder.append(daysFormat.replace("%value%", String.valueOf(days))).append(" ");
      }

      if (hours > 0L || days > 0L) {
         builder.append(hoursFormat.replace("%value%", String.valueOf(hours))).append(" ");
      }

      if (minutes > 0L || hours > 0L || days > 0L) {
         builder.append(minutesFormat.replace("%value%", String.valueOf(minutes))).append(" ");
      }

      builder.append(secondsFormat.replace("%value%", String.valueOf(seconds)));
      return builder.toString();
   }

   public static String formatSeconds(int seconds) {
      if (seconds == 0) {
         return "0:00";
      }

      if (seconds < 0) {
         return "-:--";
      }

      int minutes = seconds / 60;
      int remainingSeconds = seconds % 60;
      StringBuilder sb = new StringBuilder();
      sb.append(minutes).append(':');
      if (remainingSeconds < 10) {
         sb.append('0');
      }

      sb.append(remainingSeconds);
      return sb.toString();
   }
}
