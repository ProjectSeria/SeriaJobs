package id.seria.gacha.limbo.sql;

import id.seria.gacha.lib.common.services.services.database.DatabaseProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractQueryBuilder {
   public AbstractQueryBuilder() {
   }

   public abstract String build();

   public List<QueryResult> execute(DatabaseProvider databaseProvider, Object... objects) throws SQLException {
      List<QueryResult> resultList = new ArrayList<>();

      try (
         Connection connection = databaseProvider.getDataSource().getConnection();
         PreparedStatement stmt = connection.prepareStatement(this.build());
      ) {
         if (objects.length > 0) {
            this.setStatementParameters(stmt, objects);
         }

         try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
               QueryResult queryResult = this.processResultSetRow(rs);
               resultList.add(queryResult);
            }
         }
      }

      return resultList;
   }

   private void setStatementParameters(PreparedStatement stmt, Object... objects) throws SQLException {
      if (objects != null && objects.length != 0) {
         for (int i = 0; i < objects.length; i++) {
            stmt.setObject(i + 1, objects[i]);
         }
      } else {
         throw new IllegalArgumentException("No parameters provided for the query.");
      }
   }

   private QueryResult processResultSetRow(ResultSet rs) throws SQLException {
      Map<String, Object> resultMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();

      for (int i = 1; i <= columnCount; i++) {
         String columnName = metaData.getColumnName(i);
         Object value = rs.getObject(i);
         resultMap.put(columnName, value);
      }

      return new QueryResult(resultMap);
   }

   public abstract AbstractQueryBuilder clone();
}
