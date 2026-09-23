package id.seria.gacha.api.sdk.config;

import id.seria.gacha.api.sdk.exceptions.NotImplementedException;

public interface DatabaseConfiguration {
   static DatabaseConfiguration sqlite() {
      return new DatabaseConfiguration() {
         @Override
         public String getType() {
            return "SQLITE";
         }

         @Override
         public String getAddress() {
            return "localhost";
         }

         @Override
         public String getName() {
            return "phoenixtest_db";
         }

         @Override
         public int getPort() {
            return 3306;
         }

         @Override
         public String getUsername() {
            throw new NotImplementedException();
         }

         @Override
         public String getPassword() {
            throw new NotImplementedException();
         }

         @Override
         public String getTablePrefix() {
            return "";
         }

         @Override
         public int getMaximumPoolSize() {
            return 10;
         }

         @Override
         public int getMinimumIdle() {
            return 5;
         }

         @Override
         public long getIdleTimeout() {
            return 300000L;
         }

         @Override
         public long getMaxLifetime() {
            return 1700000L;
         }

         @Override
         public long getConnectionTimeout() {
            return 30000L;
         }

         @Override
         public long getFetchTimeout() {
            return 5000L;
         }
      };
   }

   String getType();

   String getAddress();

   String getName();

   int getPort();

   String getUsername();

   String getPassword();

   String getTablePrefix();

   int getMaximumPoolSize();

   int getMinimumIdle();

   long getIdleTimeout();

   long getMaxLifetime();

   long getConnectionTimeout();

   long getFetchTimeout();
}
