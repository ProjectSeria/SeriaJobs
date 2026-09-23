package id.seria.gacha.lib.common.services.services.database.schema;

import lombok.Generated;

public class SchemaColumn {
   private String name;
   private String type;
   private int size;

   @Override
   public String toString() {
      return String.format("SchemaColumn[name=%s, type=%s, size=%d]", this.name, this.type, this.size);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getType() {
      return this.type;
   }

   @Generated
   public int getSize() {
      return this.size;
   }

   @Generated
   public SchemaColumn(String name, String type, int size) {
      this.name = name;
      this.type = type;
      this.size = size;
   }
}
