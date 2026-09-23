package id.seria.gacha.limbo.hooks.modelengine;

import id.seria.gacha.lib.common.modelengine.interfaces.MegAPI;
import id.seria.gacha.lib.common.modelengine.interfaces.enums.MegVersion;
import id.seria.gacha.lib.common.modelengine.v4_0_3.MegAPIImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_3.MegListenerImpl;
import id.seria.gacha.lib.common.utils.api.ProductVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@Deprecated
public class ModelEngine {
   private static MegVersion VERSION;
   private static MegAPI INSTANCE;

   public ModelEngine() {
   }

   public static void initialize(JavaPlugin plugin) {
      Plugin megPlugin = Bukkit.getServer().getPluginManager().getPlugin("ModelEngine");
      if (megPlugin == null) {
         throw new IllegalStateException("ModelEngine plugin not found!");
      }

      String megVersion = extractPluginVersion(megPlugin);
      ProductVersion version403 = new ProductVersion("4.0.3");
      ProductVersion version408 = new ProductVersion("4.0.8");
      ProductVersion version409 = new ProductVersion("4.0.9");
      ProductVersion pluginVersion = new ProductVersion(megVersion);
      boolean isBetween403and408 = pluginVersion.compareTo(version403) >= 0 && pluginVersion.compareTo(version408) < 0;
      boolean isBetween408and409 = pluginVersion.compareTo(version408) >= 0 && pluginVersion.compareTo(version409) < 0;
      boolean isGreaterThanOrEqual409 = pluginVersion.compareTo(version409) >= 0;
      if (isBetween403and408) {
         VERSION = MegVersion.VER4;
         INSTANCE = new MegAPIImpl();
         Bukkit.getPluginManager().registerEvents(new MegListenerImpl(), plugin);
      } else if (isBetween408and409) {
         VERSION = MegVersion.VER4;
         INSTANCE = new id.seria.gacha.lib.common.modelengine.v4_0_8.MegAPIImpl();
         Bukkit.getPluginManager().registerEvents(new id.seria.gacha.lib.common.modelengine.v4_0_8.MegListenerImpl(), plugin);
      } else {
         if (!isGreaterThanOrEqual409) {
            throw new IllegalStateException("Model Engine " + megVersion + " found but not supported! Make sure to upgrade.");
         }

         VERSION = MegVersion.VER4;
         INSTANCE = new id.seria.gacha.lib.common.modelengine.v4_0_9.MegAPIImpl();
         Bukkit.getPluginManager().registerEvents(new id.seria.gacha.lib.common.modelengine.v4_0_9.MegListenerImpl(), plugin);
      }
   }

   private static String extractPluginVersion(Plugin modelEnginePlugin) {
      String version = modelEnginePlugin.getDescription().getVersion();
      version = version.replaceAll("[^0-9.]", "");
      if (version.startsWith(".")) {
         version = version.substring(1);
      }

      return version;
   }

   public static MegAPI getApi() {
      return INSTANCE;
   }

   public static MegVersion getVersion() {
      return VERSION;
   }
}
