package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.util;

public final class ServerVersions {
   private static Boolean isPaper = null;
   private static Boolean isFolia = null;
   private static int javaVersion = -1;

   private ServerVersions() {
   }

   public static boolean isPaper() {
      if (isPaper == null) {
         try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            isPaper = true;
         } catch (ClassNotFoundException e) {
            isPaper = false;
         }
      }

      return isPaper;
   }

   public static boolean isFolia() {
      if (isFolia == null) {
         try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
         } catch (ClassNotFoundException e) {
            isFolia = false;
         }
      }

      return isFolia;
   }

   public static int getJavaVersion() {
      if (javaVersion == -1) {
         try {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.")) {
               version = version.substring(2, 3);
            } else {
               int dot = version.indexOf(".");
               if (dot != -1) {
                  version = version.substring(0, dot);
               }
            }

            version = version.split("-")[0];
            javaVersion = Integer.parseInt(version);
         } catch (Throwable throwable) {
            throw new RuntimeException("Failed to determine Java version", throwable);
         }
      }

      return javaVersion;
   }
}
