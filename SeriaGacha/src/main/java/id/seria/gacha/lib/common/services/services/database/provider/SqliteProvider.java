package id.seria.gacha.lib.common.services.services.database.provider;

import id.seria.gacha.api.sdk.config.DatabaseConfiguration;
import id.seria.gacha.lib.common.services.services.database.DatabaseProvider;
import id.seria.gacha.lib.common.services.services.database.DatabaseType;
import id.seria.gacha.lib.hikari.HikariConfig;
import id.seria.gacha.lib.hikari.HikariDataSource;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.sql.SQLException;
import lombok.Generated;

public class SqliteProvider implements DatabaseProvider {
   private final ServerPlugin plugin;
   private final DatabaseConfiguration settings;
   private HikariDataSource dataSource;

   @Override
   public void connect() {
      if (!this.plugin.getDataFolder().exists()) {
         this.plugin.getDataFolder().mkdirs();
      }

      this.dataSource = new HikariDataSource(this.generateProperties("database"));
   }

   private HikariConfig generateProperties(String dbName) {
      HikariConfig config = new HikariConfig();
      config.setPoolName("CyberSQLitePool");
      config.setDriverClassName("org.sqlite.JDBC");
      config.setJdbcUrl("jdbc:sqlite:" + this.plugin.getDataFolder().getAbsolutePath() + File.separator + dbName + ".db");
      config.addDataSourceProperty("journal_mode", "WAL");
      config.addDataSourceProperty("busy_timeout", "5000");
      config.setConnectionTestQuery("PRAGMA user_version;");
      config.setMaxLifetime(1800000L);
      config.setIdleTimeout(600000L);
      config.setMaximumPoolSize(5);
      config.setMinimumIdle(1);
      config.setValidationTimeout(3000L);
      config.setLeakDetectionThreshold(10000L);
      return config;
   }

   @Override
   public void close() throws SQLException {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }

   @Override
   public DatabaseType getType() {
      return DatabaseType.SQLITE;
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   @Override
   public DatabaseConfiguration getSettings() {
      return this.settings;
   }

   @Generated
   @Override
   public HikariDataSource getDataSource() {
      return this.dataSource;
   }

   @Generated
   public SqliteProvider(ServerPlugin plugin, DatabaseConfiguration settings) {
      this.plugin = plugin;
      this.settings = settings;
   }
}
