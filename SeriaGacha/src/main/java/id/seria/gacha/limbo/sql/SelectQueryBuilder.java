package id.seria.gacha.limbo.sql;

import id.seria.gacha.Crates;
import java.sql.Timestamp;
import java.util.List;
import org.apache.commons.lang3.Validate;

public class SelectQueryBuilder extends AbstractQueryBuilder {
   private final List<String> columns;
   private String tableName;
   private String whereCondition = "";
   private String orderByClause;
   private String groupByClause;
   private String havingClause;
   private int limitValue = -1;
   private int offsetValue = -1;

   public SelectQueryBuilder(List<String> columns) {
      this.columns = columns;
   }

   public SelectQueryBuilder from(String tableName) {
      this.tableName = tableName;
      return this;
   }

   public SelectQueryBuilder where(String condition) {
      this.whereCondition = condition;
      return this;
   }

   private void validateCondition(String condition) {
      if (!condition.matches("(\\w+\\s*(=|<|>|<=|>=|<>|LIKE)\\s*\\?)((\\s*(AND|OR)\\s*\\w+\\s*(=|<|>|<=|>=|<>|LIKE)\\s*\\?)+)?")) {
         throw new IllegalArgumentException("Invalid condition: " + condition);
      }
   }

   public SelectQueryBuilder orderBy(String clause) {
      this.orderByClause = clause;
      return this;
   }

   public SelectQueryBuilder groupBy(String clause) {
      this.groupByClause = clause;
      return this;
   }

   public SelectQueryBuilder having(String clause) {
      this.havingClause = clause;
      return this;
   }

   public SelectQueryBuilder limit(int value) {
      this.limitValue = value;
      return this;
   }

   public SelectQueryBuilder offset(int value) {
      this.offsetValue = value;
      return this;
   }

   @Override
   public String build() {
      StringBuilder sql = new StringBuilder("SELECT ");
      if (this.columns.isEmpty()) {
         sql.append("*");
      } else {
         for (int i = 0; i < this.columns.size(); i++) {
            sql.append(this.columns.get(i));
            if (i < this.columns.size() - 1) {
               sql.append(", ");
            }
         }
      }

      sql.append(" FROM ").append(this.getFormattedTableName());
      if (this.whereCondition != null && !this.whereCondition.isEmpty()) {
         sql.append(" WHERE ").append(this.whereCondition);
      }

      if (this.groupByClause != null) {
         sql.append(" GROUP BY ").append(this.groupByClause);
      }

      if (this.havingClause != null) {
         sql.append(" HAVING ").append(this.havingClause);
      }

      if (this.orderByClause != null) {
         sql.append(" ORDER BY ").append(this.orderByClause);
      }

      if (this.limitValue != -1) {
         sql.append(" LIMIT ").append(this.limitValue);
      }

      if (this.offsetValue != -1) {
         sql.append(" OFFSET ").append(this.offsetValue);
      }

      return sql.toString();
   }

   private String getFormattedTableName() {
      String finalName = this.tableName;
      if (this.tableName.contains("%prefix%")) {
         String tablePrefix = Crates.getConfiguration().getDatabase().getTablePrefix();
         Validate.notNull(tablePrefix, "Table prefix is null", new Object[0]);
         finalName = finalName.replace("%prefix%", tablePrefix);
      }

      return finalName.replaceAll("^_+|_+$", "");
   }

   public String stringify(Object... values) {
      String query = this.build();

      for (Object value : values) {
         String converted = String.valueOf(value instanceof Timestamp ? ((Timestamp)value).getTime() : value);
         query = query.replaceFirst("\\?", converted);
      }

      return query;
   }

   public SelectQueryBuilder clone() {
      SelectQueryBuilder builder = new SelectQueryBuilder(this.columns);
      builder.tableName = this.tableName;
      builder.whereCondition = this.whereCondition;
      builder.orderByClause = this.orderByClause;
      builder.groupByClause = this.groupByClause;
      builder.havingClause = this.havingClause;
      builder.limitValue = this.limitValue;
      builder.offsetValue = this.offsetValue;
      return builder;
   }
}
