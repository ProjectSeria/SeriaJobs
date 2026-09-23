package id.seria.gacha.lib.common.services.services.database;

import id.seria.gacha.api.sdk.config.DatabaseConfiguration;
import id.seria.gacha.lib.common.services.services.database.context.annotations.DatabaseTable;
import id.seria.gacha.lib.hikari.HikariDataSource;
import java.sql.SQLException;
import org.apache.commons.lang.Validate;

public interface DatabaseProvider {
   void connect();

   DatabaseConfiguration getSettings();

   HikariDataSource getDataSource();

   void close() throws SQLException;

   DatabaseType getType();

   default String formatTableName(DatabaseTable annotation) {
      return this.formatTableName(annotation.name());
   }

   default String formatTableName(String rawTableName) {
      if (rawTableName.contains("%prefix%")) {
         Validate.notNull(this.getSettings().getTablePrefix(), "Table prefix is null");
         rawTableName = rawTableName.replace("%prefix%", this.getSettings().getTablePrefix());
      }

      return rawTableName.replaceAll("^_+|_+$", "");
   }
}
