package id.seria.gacha.lib.common.services.services;

import id.seria.gacha.api.sdk.config.DatabaseConfiguration;
import id.seria.gacha.lib.common.services.factory.ServicesFactory;
import id.seria.gacha.lib.common.services.services.database.DatabaseProvider;
import id.seria.gacha.lib.common.services.services.database.DatabaseType;
import id.seria.gacha.lib.common.services.services.database.context.DatabaseRepository;
import id.seria.gacha.lib.common.services.services.database.provider.MariaDBProvider;
import id.seria.gacha.lib.common.services.services.database.provider.MysqlProvider;
import id.seria.gacha.lib.common.services.services.database.provider.SqliteProvider;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseService implements ServicesFactory.ServiceLifecycle, Listener {
   private static final long DB_THREAD_POOL_DRAIN_TIMEOUT_SECONDS = 60L;
   private static final long DB_THREAD_POOL_FORCE_TIMEOUT_SECONDS = 10L;
   private final ServerPlugin plugin;
   private Map<JavaPlugin, DatabaseProvider> providers = new HashMap<>();

   public DatabaseService(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void load() throws Exception {
      this.plugin.getEventBus().registerListener(this);
   }

   @Override
   public void unload() throws Exception {
      this.drainThreadPool();

      for (DatabaseProvider provider : this.providers.values()) {
         provider.close();
      }

      this.plugin.getEventBus().unregisterListener(this);
   }

   public DatabaseProvider createProvider(ServerPlugin plugin, DatabaseConfiguration configuration) throws Exception {
      DatabaseProvider provider = null;

      DatabaseType type;
      try {
         type = DatabaseType.valueOf(configuration.getType().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("Unknown database type! " + configuration.getType() + "??");
      }

      switch (type) {
         case SQLITE:
            provider = new SqliteProvider(plugin, configuration);
            provider.connect();
            break;
         case MYSQL:
            provider = new MysqlProvider(configuration);
            provider.connect();
            break;
         case MARIADB:
            provider = new MariaDBProvider(configuration);
            provider.connect();
            break;
         default:
            throw new IllegalArgumentException("Database type \"" + type.name() + "\" not configured! ");
      }

      return provider;
   }

   @EventHandler
   protected void onPluginDisable(PluginDisableEvent event) {
      if (event.getPlugin() instanceof JavaPlugin) {
         JavaPlugin plugin = (JavaPlugin)event.getPlugin();
         synchronized (this.providers) {
            DatabaseProvider provider = this.providers.remove(plugin);
            if (provider != null) {
               this.drainThreadPool();

               try {
                  provider.close();
               } catch (SQLException e) {
                  e.printStackTrace();
               }
            }
         }
      }
   }

   private void drainThreadPool() {
      if (DatabaseRepository.THREAD_POOL != null && !DatabaseRepository.THREAD_POOL.isShutdown()) {
         DatabaseRepository.THREAD_POOL.shutdown();

         try {
            if (!DatabaseRepository.THREAD_POOL.awaitTermination(60L, TimeUnit.SECONDS)) {
               DatabaseRepository.THREAD_POOL.shutdownNow();
               DatabaseRepository.THREAD_POOL.awaitTermination(10L, TimeUnit.SECONDS);
            }
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<JavaPlugin, DatabaseProvider> getProviders() {
      return this.providers;
   }
}
