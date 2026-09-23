package id.seria.gacha.limbo.sql;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Generated;

public class QueryResult {
   private final Map<String, Object> resultMap;

   public QueryResult(Map<String, Object> resultMap) {
      this.resultMap = resultMap;
   }

   public String getString(String columnName) {
      return this.getResultValue(columnName, String.class);
   }

   public boolean getBoolean(String columnName) {
      return this.getResultValue(columnName, Boolean.class);
   }

   public int getInt(String columnName) {
      Object value = this.resultMap.get(columnName);
      if (value instanceof BigDecimal) {
         return ((BigDecimal)value).intValue();
      } else {
         return value instanceof Long ? ((Long)value).intValue() : (Integer)value;
      }
   }

   public long getLong(String columnName) {
      Object value = this.resultMap.get(columnName);
      if (value == null) {
         return 0L;
      } else if (value instanceof BigDecimal) {
         return ((BigDecimal)value).longValue();
      } else if (value instanceof Integer) {
         return ((Integer)value).longValue();
      } else {
         return value instanceof Long ? (Long)value : Long.parseLong(String.valueOf(value));
      }
   }

   public double getDouble(String columnName) {
      return this.getResultValue(columnName, Double.class);
   }

   public Object getObject(String columnName) {
      return this.resultMap.get(columnName);
   }

   private <T> T getResultValue(String columnName, Class<T> type) {
      Object value = this.resultMap.get(columnName);
      return value == null ? null : type.cast(value);
   }

   @Generated
   @Override
   public String toString() {
      return "QueryResult(resultMap=" + this.resultMap + ")";
   }
}
