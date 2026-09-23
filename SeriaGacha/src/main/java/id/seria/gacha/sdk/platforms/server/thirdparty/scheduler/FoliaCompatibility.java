package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FoliaCompatibility {
   @NotNull
   private final ServerImplementation serverImplementation;

   public FoliaCompatibility(@NotNull Plugin plugin) {
      assertRelocated(plugin);

      ServerImplementation scheduler;
      try {
         try {
            Server.class.getMethod("isOwnedByCurrentRegion", Location.class);
            scheduler = Class.forName(this.getClass().getPackage().getName() + ".folia.FoliaServer")
               .asSubclass(ServerImplementation.class)
               .getConstructor(Plugin.class)
               .newInstance(plugin);
         } catch (NoSuchMethodException ex) {
            scheduler = Class.forName(this.getClass().getPackage().getName() + ".bukkit.BukkitServer")
               .asSubclass(ServerImplementation.class)
               .getConstructor(Plugin.class)
               .newInstance(plugin);
         }
      } catch (Throwable ex) {
         throw new RuntimeException("Failed to initialize scheduler", ex);
      }

      this.serverImplementation = scheduler;
   }

   @NotNull
   public ServerImplementation getServerImplementation() {
      return this.serverImplementation;
   }

   private static void assertRelocated(@NotNull Plugin plugin) {
      String originalPackage = String.join(".", "com", "cjcrafter", "scheduler");
      String relocatedPackage = FoliaCompatibility.class.getPackage().getName();
      if (originalPackage.equals(relocatedPackage)) {
         String authors = String.join(", ", plugin.getDescription().getAuthors());
         String possiblePackage = plugin.getClass().getPackage().getName() + ".libs.scheduler";
         plugin.getLogger().warning("The FoliaScheduler lib has not been relocated!");
         plugin.getLogger().warning("The package is still in '" + originalPackage + "'");
         plugin.getLogger().warning("Please relocate the lib to '" + possiblePackage + "'");
         plugin.getLogger().warning("Please warn authors " + authors + " about this issue.");
      }
   }
}
