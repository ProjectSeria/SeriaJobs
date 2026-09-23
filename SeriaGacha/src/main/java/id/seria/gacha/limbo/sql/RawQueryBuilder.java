package id.seria.gacha.limbo.sql;

public class RawQueryBuilder extends AbstractQueryBuilder {
   private final String query;

   public RawQueryBuilder(String query) {
      this.query = query;
   }

   @Override
   public String build() {
      return this.query;
   }

   public RawQueryBuilder clone() {
      return new RawQueryBuilder(this.query);
   }
}
