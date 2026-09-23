package id.seria.gacha.limbo.sql;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class QueryBuilder {
   public static AbstractQueryBuilder raw(String rawQuery) {
      return new RawQueryBuilder(rawQuery);
   }

   public static SelectQueryBuilder select(String... options) {
      List<String> columns = new ArrayList<>();
      if (options.length > 0) {
         for (String option : options) {
            if (option != null) {
               if (option.equals("*")) {
                  columns.clear();
                  break;
               }

               columns.add(option);
            }
         }
      }

      return new SelectQueryBuilder(columns);
   }

   @Generated
   public QueryBuilder() {
   }
}
