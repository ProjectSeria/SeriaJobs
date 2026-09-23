package id.seria.gacha.lib.common.utils.version;

import org.bukkit.Bukkit;

public class VersionUtils {
   private static final String[] PAPER_SERVER_NAMES = new String[]{"Paper", "Folia", "Purpur", "Leaf", "UniverseSpigot", "Pufferfish", "AdvancedSlimePaper"};
   private static final String[] PAPER_MARKER_CLASSES = new String[]{
      "io.papermc.paper.threadedregions.RegionizedServer",
      "io.papermc.paper.ServerBuildInfo",
      "io.papermc.paper.configuration.Configuration",
      "com.destroystokyo.paper.PaperConfig"
   };
   private static final boolean IS_PAPER_SERVER = detectPaperServer();
   private static Boolean IS_FOLIA_SERVER = null;

   public VersionUtils() {
   }

   private static boolean detectPaperServer() {
      String name = Bukkit.getName();

      for (String paperName : PAPER_SERVER_NAMES) {
         if (paperName.equalsIgnoreCase(name)) {
            return true;
         }
      }

      for (String markerClass : PAPER_MARKER_CLASSES) {
         if (hasClass(markerClass)) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasClass(String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException | LinkageError e) {
         return false;
      }
   }

   public static boolean isPaperServer() {
      return IS_PAPER_SERVER;
   }

   public static boolean isFoliaServer() {
      if (IS_FOLIA_SERVER == null) {
         IS_FOLIA_SERVER = hasClass("io.papermc.paper.threadedregions.RegionizedServer");
      }

      return IS_FOLIA_SERVER;
   }

   public static String extractNMSVersion() {
      String fullVersion = Bukkit.getServer().getBukkitVersion();
      String nms = fullVersion.split("-")[0];
      if (Bukkit.getBukkitVersion().contains("26.2")) {
         return "v26_R2";
      } else if (Bukkit.getBukkitVersion().contains("26.1") || Bukkit.getBukkitVersion().contains("26")) {
         return "v26_R1";
      } else if (Bukkit.getBukkitVersion().contains("1.21.11")) {
         return "v1_21_R11";
      } else if (Bukkit.getBukkitVersion().contains("1.21.10")) {
         return "v1_21_R10";
      } else if (Bukkit.getBukkitVersion().contains("1.21.9")) {
         return "v1_21_R9";
      } else if (Bukkit.getBukkitVersion().contains("1.21.7") || Bukkit.getBukkitVersion().contains("1.21.8")) {
         return "v1_21_R7";
      } else if (Bukkit.getBukkitVersion().contains("1.21.6")) {
         return "v1_21_R6";
      } else if (Bukkit.getBukkitVersion().contains("1.21.5")) {
         return "v1_21_R5";
      } else if (Bukkit.getBukkitVersion().contains("1.21.4")) {
         return "v1_21_R4";
      } else if (Bukkit.getBukkitVersion().contains("1.21.3")) {
         return "v1_21_R3";
      } else if (Bukkit.getBukkitVersion().contains("1.21.2")) {
         return "v1_21_R2";
      } else if (Bukkit.getBukkitVersion().contains("1.21.1") || Bukkit.getBukkitVersion().contains("1.21")) {
         return "v1_21_R1";
      } else if (Bukkit.getBukkitVersion().contains("1.20.4")) {
         return "v1_20_R4";
      } else {
         return !nms.contains("1.20.5") && !nms.contains("1.20.6") ? null : "v1_20_R6";
      }
   }

   private static int compare(String reference, String comparison) throws NumberFormatException {
      String[] referenceSplit = reference.split("\\.");
      String[] comparisonSplit = comparison.split("\\.");
      int longest = Math.max(referenceSplit.length, comparisonSplit.length);
      int[] referenceNumbersArray = new int[longest];
      int[] comparisonNumbersArray = new int[longest];

      for (int i = 0; i < referenceSplit.length; i++) {
         referenceNumbersArray[i] = Integer.parseInt(referenceSplit[i]);
      }

      for (int i = 0; i < comparisonSplit.length; i++) {
         comparisonNumbersArray[i] = Integer.parseInt(comparisonSplit[i]);
      }

      for (int i = 0; i < longest; i++) {
         int diff = referenceNumbersArray[i] - comparisonNumbersArray[i];
         if (diff > 0) {
            return 1;
         }

         if (diff < 0) {
            return -1;
         }
      }

      return 0;
   }

   public static boolean isVersionGreaterEqual(String reference, String thanWhat) {
      return compare(reference, thanWhat) >= 0;
   }

   public static boolean isVersionLessEqual(String reference, String thanWhat) {
      return compare(reference, thanWhat) <= 0;
   }

   public static boolean isVersionBetweenEqual(String reference, String lowest, String highest) {
      return isVersionGreaterEqual(reference, lowest) && isVersionLessEqual(reference, highest);
   }
}
