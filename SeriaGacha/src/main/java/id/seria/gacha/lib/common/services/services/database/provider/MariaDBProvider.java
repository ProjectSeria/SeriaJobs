package id.seria.gacha.lib.common.services.services.database.provider;

import id.seria.gacha.api.sdk.config.DatabaseConfiguration;
import id.seria.gacha.lib.common.services.services.database.DatabaseProvider;
import id.seria.gacha.lib.common.services.services.database.DatabaseType;
import id.seria.gacha.lib.hikari.HikariConfig;
import id.seria.gacha.lib.hikari.HikariDataSource;
import java.sql.SQLException;
import lombok.Generated;

public class MariaDBProvider implements DatabaseProvider {
   private final DatabaseConfiguration settings;
   private HikariDataSource dataSource;

   @Override
   public void connect() {
      this.dataSource = new HikariDataSource(this.generateProperties());
   }

   private HikariConfig generateProperties() {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(
         "jdbc:mysql://"
            + this.settings.getAddress()
            + ":"
            + this.settings.getPort()
            + "/"
            + this.settings.getName()
            + "?createDatabaseIfNotExist=true&useSSL=false&characterEncoding=UTF-8"
      );
      config.setUsername(this.settings.getUsername());
      config.setPassword(this.settings.getPassword());
      config.addDataSourceProperty("cachePrepStmts", true);
      config.addDataSourceProperty("prepStmtCacheSize", 250);
      config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
      config.addDataSourceProperty("useServerPrepStmts", true);
      config.addDataSourceProperty("cacheResultSetMetadata", true);
      config.addDataSourceProperty("tcpKeepAlive", true);
      config.setMaximumPoolSize(this.settings.getMaximumPoolSize());
      config.setMinimumIdle(this.settings.getMinimumIdle());
      config.setIdleTimeout(this.settings.getIdleTimeout());
      config.setMaxLifetime(this.settings.getMaxLifetime());
      config.setConnectionTimeout(this.settings.getConnectionTimeout());
      config.setLeakDetectionThreshold(60000L);
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
      return DatabaseType.MARIADB;
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
   public MariaDBProvider(DatabaseConfiguration settings) {
      this.settings = settings;
   }
}
