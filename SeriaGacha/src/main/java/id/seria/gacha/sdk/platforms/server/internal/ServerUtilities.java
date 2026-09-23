package id.seria.gacha.sdk.platforms.server.internal;

import id.seria.gacha.sdk.utilities.Cache;

public class ServerUtilities {
   public ServerUtilities() {
   }

   public static boolean isFoliaServer() {
      return Cache.fetch("server.folia-present", () -> {
         try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
         } catch (ClassNotFoundException e) {
            return false;
         }
      });
   }
}
