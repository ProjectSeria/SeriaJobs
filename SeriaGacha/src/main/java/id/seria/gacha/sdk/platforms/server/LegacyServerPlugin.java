package id.seria.gacha.sdk.platforms.server;

import id.seria.gacha.api.sdk.Platform;
import id.seria.gacha.api.sdk.config.DatabaseConfiguration;
import id.seria.gacha.api.sdk.config.PluginConfiguration;
import id.seria.gacha.sdk.platforms.server.commands.ServerCommandManager;
import id.seria.gacha.sdk.utilities.exceptions.NotImplementedException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class LegacyServerPlugin extends ServerPlugin {
   public static LegacyServerPlugin wrap(JavaPlugin plugin) {
      try {
         return new LegacyServerPlugin(plugin);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private LegacyServerPlugin(JavaPlugin plugin) throws Exception {
      super(plugin);
   }

   @Override
   public void onInit() throws Exception {
   }

   @Override
   public void onLoad() throws Exception {
   }

   @Override
   public void onUnload() throws Exception {
   }

   @Override
   public void onReload() throws Exception {
   }

   @Override
   public boolean isDebugEnabled() {
      return false;
   }

   @Override
   public Platform getPlatform() {
      throw new NotImplementedException();
   }

   @NotNull
   @Override
   public ServerCommandManager getCommandManager() {
      throw new NotImplementedException();
   }

   @Override
   public PluginConfiguration getConfiguration() {
      return new PluginConfiguration() {
         @Override
         public int getInternalPoolSize() {
            return 10;
         }

         @Override
         public DatabaseConfiguration getDatabase() {
            return DatabaseConfiguration.sqlite();
         }
      };
   }
}
