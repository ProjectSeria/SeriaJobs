package id.seria.gacha.configurations;

import id.seria.gacha.api.sdk.config.DatabaseConfiguration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import lombok.Generated;

public class DatabaseConfigurationSettings implements DatabaseConfiguration {
   @ConfigField(path = "type")
   private String type = "sqlite";
   @ConfigField(path = "address")
   private String address = "localhost";
   @ConfigField(path = "name")
   private String name = "phoenixcrates_db";
   @ConfigField(path = "port")
   private int port = 3306;
   @ConfigField(path = "username")
   private String username = "phoenixuser";
   @ConfigField(path = "password")
   private String password = "phoenixpassword";
   @ConfigField(path = "table-prefix")
   private String tablePrefix = "pc";
   @ConfigField(path = "hikari.maximumPoolSize")
   private int maximumPoolSize = 20;
   @ConfigField(path = "hikari.minimumIdle")
   private int minimumIdle = 5;
   @ConfigField(path = "hikari.idleTimeout")
   private long idleTimeout = 600000L;
   @ConfigField(path = "hikari.maxLifetime")
   private long maxLifetime = 1800000L;
   @ConfigField(path = "hikari.connectionTimeout")
   private long connectionTimeout = 30000L;
   @ConfigField(path = "hikari.fetch-timeout")
   private long fetchTimeout = 5000L;

   @Generated
   public void setType(String type) {
      this.type = type;
   }

   @Generated
   public void setAddress(String address) {
      this.address = address;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setPort(int port) {
      this.port = port;
   }

   @Generated
   public void setUsername(String username) {
      this.username = username;
   }

   @Generated
   public void setPassword(String password) {
      this.password = password;
   }

   @Generated
   public void setTablePrefix(String tablePrefix) {
      this.tablePrefix = tablePrefix;
   }

   @Generated
   public void setMaximumPoolSize(int maximumPoolSize) {
      this.maximumPoolSize = maximumPoolSize;
   }

   @Generated
   public void setMinimumIdle(int minimumIdle) {
      this.minimumIdle = minimumIdle;
   }

   @Generated
   public void setIdleTimeout(long idleTimeout) {
      this.idleTimeout = idleTimeout;
   }

   @Generated
   public void setMaxLifetime(long maxLifetime) {
      this.maxLifetime = maxLifetime;
   }

   @Generated
   public void setConnectionTimeout(long connectionTimeout) {
      this.connectionTimeout = connectionTimeout;
   }

   @Generated
   public void setFetchTimeout(long fetchTimeout) {
      this.fetchTimeout = fetchTimeout;
   }

   @Generated
   @Override
   public String getType() {
      return this.type;
   }

   @Generated
   @Override
   public String getAddress() {
      return this.address;
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   @Override
   public int getPort() {
      return this.port;
   }

   @Generated
   @Override
   public String getUsername() {
      return this.username;
   }

   @Generated
   @Override
   public String getPassword() {
      return this.password;
   }

   @Generated
   @Override
   public String getTablePrefix() {
      return this.tablePrefix;
   }

   @Generated
   @Override
   public int getMaximumPoolSize() {
      return this.maximumPoolSize;
   }

   @Generated
   @Override
   public int getMinimumIdle() {
      return this.minimumIdle;
   }

   @Generated
   @Override
   public long getIdleTimeout() {
      return this.idleTimeout;
   }

   @Generated
   @Override
   public long getMaxLifetime() {
      return this.maxLifetime;
   }

   @Generated
   @Override
   public long getConnectionTimeout() {
      return this.connectionTimeout;
   }

   @Generated
   @Override
   public long getFetchTimeout() {
      return this.fetchTimeout;
   }

   @Generated
   public DatabaseConfigurationSettings() {
   }
}
