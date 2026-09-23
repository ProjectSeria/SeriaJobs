package id.seria.gacha.lib.common.services.services.database;

import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;
import id.seria.gacha.lib.common.services.services.database.context.annotations.DatabaseField;
import id.seria.gacha.lib.common.services.services.database.context.annotations.DatabaseTable;
import id.seria.gacha.lib.common.services.services.database.context.annotations.MediumText;
import id.seria.gacha.lib.common.services.services.database.context.annotations.keys.ForeignKey;
import id.seria.gacha.lib.common.services.services.database.context.annotations.keys.PrimaryKey;
import id.seria.gacha.lib.common.services.services.database.context.annotations.keys.Unique;
import id.seria.gacha.lib.common.services.services.database.schema.SchemaColumn;
import id.seria.gacha.lib.common.utils.utility.quaternion.MathUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.WordUtils;

public class DatabaseORM {
   public static final Map<Class<?>, String> lookupMap = Maps.newHashMap();

   public DatabaseORM() {
   }

   public static String createSqliteTableQuery(DatabaseProvider provider, Class<?> tableClass) {
      if (!tableClass.isAnnotationPresent(DatabaseTable.class)) {
         throw new IllegalArgumentException("Cannot create a create query from a not-queryable class.");
      }

      StringBuilder sb = new StringBuilder();
      DatabaseTable tableAnnotation = tableClass.getAnnotation(DatabaseTable.class);
      sb.append("CREATE TABLE ");
      if (tableAnnotation.unique()) {
         sb.append("IF NOT EXISTS ");
      }

      sb.append("`").append(provider.formatTableName(tableAnnotation)).append("` (");
      int index = 0;
      List<Field> ignoredFields = new ArrayList<>();

      for (Field field : tableClass.getDeclaredFields()) {
         if (field.isAnnotationPresent(DatabaseField.class)) {
            DatabaseField fieldAnnotation = field.getAnnotation(DatabaseField.class);
            Class<?> type = fieldAnnotation.type().equals(Object.class) ? Primitives.wrap(field.getType()) : fieldAnnotation.type();
            String dataType = lookupMap.get(type);
            if (dataType == null) {
               throw new IllegalArgumentException("Field with a invalid type: " + type);
            }

            if (index > 0) {
               sb.append(" ");
            }

            sb.append("`").append(WordUtils.capitalize(field.getName())).append("` ");
            sb.append(getSqlDataType(provider, field));
            if (fieldAnnotation.unique()) {
               sb.append(" UNIQUE");
            }

            if (fieldAnnotation.autoIncrement()) {
               if (field.isAnnotationPresent(PrimaryKey.class)) {
                  ignoredFields.add(field);
                  sb.append(" PRIMARY KEY");
               }

               sb.append(" AUTOINCREMENT");
            } else if (!fieldAnnotation.nullable()) {
               sb.append(" NOT NULL");
            }

            sb.append(",");
            index++;
         }
      }

      for (Field field : tableClass.getDeclaredFields()) {
         if (!ignoredFields.contains(field)) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
               sb.append(" PRIMARY KEY(`").append(WordUtils.capitalize(field.getName())).append("`),");
            } else if (field.isAnnotationPresent(ForeignKey.class)) {
               ForeignKey key = field.getAnnotation(ForeignKey.class);
               if (!key.modelClass().isAnnotationPresent(DatabaseTable.class)) {
                  throw new IllegalArgumentException("Cannot create a foreign key from a non-table class.");
               }

               DatabaseTable table = key.modelClass().getAnnotation(DatabaseTable.class);
               sb.append(" FOREIGN KEY(`")
                  .append(WordUtils.capitalize(field.getName()))
                  .append("`) REFERENCES ")
                  .append(provider.formatTableName(table))
                  .append("(`")
                  .append(WordUtils.capitalize(key.modelField()))
                  .append("`)")
                  .append(",");
            } else if (field.isAnnotationPresent(Unique.class)) {
               sb.append(" UNIQUE(`").append(WordUtils.capitalize(field.getName())).append("`),");
            }
         }
      }

      sb.setLength(sb.length() - 1);
      return sb.toString().trim() + ")";
   }

   public static String createSqliteTableQuery(DatabaseProvider provider, Class<?> tableClass, Map<String, SchemaColumn> legacyColumns) {
      if (legacyColumns != null && !legacyColumns.isEmpty()) {
         StringBuilder legacySb = new StringBuilder();

         for (Entry<String, SchemaColumn> entry : legacyColumns.entrySet()) {
            SchemaColumn col = entry.getValue();
            if (legacySb.length() > 0) {
               legacySb.append(", ");
            }

            legacySb.append("`").append(col.getName()).append("` ").append(col.getType());
         }

         String base = createSqliteTableQuery(provider, tableClass);
         String content = base.substring(0, base.length() - 1);
         int insertPos = content.length();

         for (String marker : new String[]{" PRIMARY KEY(", " FOREIGN KEY(", " UNIQUE("}) {
            int idx = content.indexOf(marker);
            if (idx != -1 && idx < insertPos) {
               insertPos = idx;
            }
         }

         String before = content.substring(0, insertPos);
         String after = content.substring(insertPos);
         String sep = before.endsWith(",") ? " " : ", ";
         String suffix = after.isEmpty() ? "" : ",";
         return before + sep + legacySb + suffix + after + ")";
      } else {
         return createSqliteTableQuery(provider, tableClass);
      }
   }

   public static String createMysqlTableQuery(DatabaseProvider provider, Class<?> tableClass) {
      if (!tableClass.isAnnotationPresent(DatabaseTable.class)) {
         throw new IllegalArgumentException("Cannot create a create query from a not-queryable class.");
      }

      StringBuilder sb = new StringBuilder();
      DatabaseTable tableAnnotation = tableClass.getAnnotation(DatabaseTable.class);
      sb.append("CREATE TABLE ");
      if (tableAnnotation.unique()) {
         sb.append("IF NOT EXISTS ");
      }

      sb.append("`").append(provider.formatTableName(tableAnnotation)).append("` (");
      int index = 0;

      for (Field field : tableClass.getDeclaredFields()) {
         if (field.isAnnotationPresent(DatabaseField.class)) {
            Class<?> type = Primitives.wrap(field.getType());
            String dataType = lookupMap.get(type);
            if (dataType == null) {
               throw new IllegalArgumentException("Field with a invalid type: " + type);
            }

            DatabaseField tableField = field.getAnnotation(DatabaseField.class);
            if (index > 0) {
               sb.append(" ");
            }

            sb.append("`").append(WordUtils.capitalize(field.getName())).append("` ");
            sb.append(getSqlDataType(provider, field));
            if (tableField.unique()) {
               sb.append(" UNIQUE");
            }

            if (!tableField.nullable()) {
               sb.append(" NOT NULL");
            }

            if (tableField.autoIncrement()) {
               sb.append(" AUTO_INCREMENT");
            }

            sb.append(",");
            index++;
         }
      }

      for (Field field : tableClass.getDeclaredFields()) {
         if (field.isAnnotationPresent(PrimaryKey.class)) {
            sb.append(" PRIMARY KEY(`").append(WordUtils.capitalize(field.getName())).append("`),");
         } else if (field.isAnnotationPresent(ForeignKey.class)) {
            ForeignKey key = field.getAnnotation(ForeignKey.class);
            if (!key.modelClass().isAnnotationPresent(DatabaseTable.class)) {
               throw new IllegalArgumentException("Cannot create a foreign key from a non-table class.");
            }

            DatabaseTable table = key.modelClass().getAnnotation(DatabaseTable.class);
            sb.append(" FOREIGN KEY(`")
               .append(WordUtils.capitalize(field.getName()))
               .append("`) REFERENCES ")
               .append(provider.formatTableName(table))
               .append("(`")
               .append(WordUtils.capitalize(key.modelField()))
               .append("`)")
               .append(",");
         } else if (field.isAnnotationPresent(Unique.class)) {
            sb.append(" UNIQUE(`").append(WordUtils.capitalize(field.getName())).append("`),");
         }
      }

      sb.setLength(sb.length() - 1);
      return sb.toString().trim() + ") ENGINE=InnoDB;";
   }

   public static String createInsertQuery(DatabaseProvider provider, Class<?> tableClass) {
      if (!tableClass.isAnnotationPresent(DatabaseTable.class)) {
         throw new IllegalArgumentException("Cannot create a create query from a not-queryable class.");
      }

      StringBuilder sb = new StringBuilder();
      DatabaseTable tableAnnotation = tableClass.getAnnotation(DatabaseTable.class);
      sb.append("INSERT INTO ");
      sb.append("`").append(provider.formatTableName(tableAnnotation)).append("` (");
      int index = 0;

      for (Field field : tableClass.getDeclaredFields()) {
         if (field.isAnnotationPresent(DatabaseField.class)) {
            DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
            if (!databaseField.autoIncrement()) {
               if (index > 0) {
                  sb.append(" ");
               }

               sb.append("`").append(WordUtils.capitalize(field.getName())).append("`");
               sb.append(",");
               index++;
            }
         }
      }

      sb.setLength(sb.length() - 1);
      sb.append(") VALUES(");
      String[] questionMarks = new String[index];
      Arrays.fill(questionMarks, "?");
      sb.append(String.join(", ", questionMarks));
      return sb.toString().trim() + ");";
   }

   public static String createUpdateQuery(DatabaseProvider provider, Class<?> tableClass) {
      if (!tableClass.isAnnotationPresent(DatabaseTable.class)) {
         throw new IllegalArgumentException("Cannot create a update query from a not-queryable class.");
      }

      StringBuilder sb = new StringBuilder();
      DatabaseTable tableAnnotation = tableClass.getAnnotation(DatabaseTable.class);
      sb.append("UPDATE ");
      sb.append("`").append(provider.formatTableName(tableAnnotation)).append("` ");
      sb.append("SET ");
      int index = 0;

      for (Field field : tableClass.getDeclaredFields()) {
         if (field.isAnnotationPresent(DatabaseField.class)) {
            DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
            if (!databaseField.autoIncrement()) {
               if (index > 0) {
                  sb.append(" ");
               }

               sb.append("`").append(WordUtils.capitalize(field.getName())).append("`");
               sb.append("=?,");
               index++;
            }
         }
      }

      sb.setLength(sb.length() - 1);
      sb.append(" WHERE");

      for (Field field : tableClass.getDeclaredFields()) {
         if (field.isAnnotationPresent(PrimaryKey.class)) {
            sb.append(" `").append(WordUtils.capitalize(field.getName())).append("`=? AND");
         }
      }

      sb.setLength(sb.length() - 4);
      return sb.toString().trim() + ";";
   }

   public static String createDeleteQuery(DatabaseProvider provider, Class<?> tableClass) {
      if (!tableClass.isAnnotationPresent(DatabaseTable.class)) {
         throw new IllegalArgumentException("Cannot create a delete query from a not-queryable class.");
      }

      StringBuilder sb = new StringBuilder();
      DatabaseTable tableAnnotation = tableClass.getAnnotation(DatabaseTable.class);
      sb.append("DELETE FROM ");
      sb.append("`").append(provider.formatTableName(tableAnnotation)).append("`");
      sb.append(" WHERE");

      for (Field field : tableClass.getDeclaredFields()) {
         if (field.isAnnotationPresent(PrimaryKey.class)) {
            sb.append(" `").append(WordUtils.capitalize(field.getName())).append("`=? AND");
         }
      }

      sb.setLength(sb.length() - 4);
      return sb.toString().trim() + ";";
   }

   public static String createSelectQuery(DatabaseProvider provider, Class<?> tableClass, String... keys) {
      if (!tableClass.isAnnotationPresent(DatabaseTable.class)) {
         throw new IllegalArgumentException("Cannot create a delete query from a not-queryable class.");
      }

      DatabaseTable tableAnnotation = tableClass.getAnnotation(DatabaseTable.class);
      StringBuilder sb = new StringBuilder();
      sb.append("SELECT * FROM ");
      sb.append("`").append(provider.formatTableName(tableAnnotation)).append("`");
      if (keys.length > 0) {
         sb.append(" WHERE");

         for (String key : keys) {
            sb.append(" `").append(key).append("`=? AND");
         }

         sb.setLength(sb.length() - 4);
      }

      return sb.toString().trim() + ";";
   }

   public static String createMysqlModifyColumnQuery(DatabaseProvider provider, Class<?> tableClass, Field field) {
      if (!tableClass.isAnnotationPresent(DatabaseTable.class)) {
         throw new IllegalArgumentException("Cannot create a delete query from a not-queryable class.");
      }

      if (!field.isAnnotationPresent(DatabaseField.class)) {
         throw new IllegalArgumentException("This field is not a database field.");
      }

      DatabaseTable tableAnnotation = tableClass.getAnnotation(DatabaseTable.class);
      DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
      StringBuilder sb = new StringBuilder();
      sb.append("ALTER TABLE ")
         .append(provider.formatTableName(tableAnnotation))
         .append(" MODIFY COLUMN `")
         .append(WordUtils.capitalize(field.getName()))
         .append("` ");
      Class<?> type = Primitives.wrap(field.getType());
      sb.append(getSqlDataType(provider, field));
      return sb.toString().trim() + ";";
   }

   public static String createAddColumnQuery(DatabaseProvider provider, Class<?> tableClass, Field field) {
      if (!tableClass.isAnnotationPresent(DatabaseTable.class)) {
         throw new IllegalArgumentException("Cannot create a delete query from a not-queryable class.");
      }

      if (!field.isAnnotationPresent(DatabaseField.class)) {
         throw new IllegalArgumentException("This field is not a database field.");
      }

      DatabaseTable tableAnnotation = tableClass.getAnnotation(DatabaseTable.class);
      DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
      StringBuilder sb = new StringBuilder();
      sb.append("ALTER TABLE ").append(provider.formatTableName(tableAnnotation)).append(" ADD `").append(WordUtils.capitalize(field.getName())).append("` ");
      sb.append(getSqlDataType(provider, field));
      return sb.toString().trim() + ";";
   }

   public static String getSqlDataType(DatabaseProvider provider, Field field) {
      if (!field.isAnnotationPresent(DatabaseField.class)) {
         throw new IllegalArgumentException("This field is not a database field.");
      }

      DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
      Class<?> type = Primitives.wrap(field.getType());
      if (type.equals(String.class)) {
         if (provider.getType() != DatabaseType.SQLITE && field.getAnnotation(MediumText.class) != null) {
            return "MEDIUMTEXT";
         }

         if (databaseField.size() >= 65535) {
            return "TEXT";
         }
      }

      StringBuilder sb = new StringBuilder();
      String dataType = lookupMap.get(type);
      sb.append(dataType);
      if (databaseField.size() > 0) {
         if (dataType.equalsIgnoreCase("INTEGER")) {
            sb.append("(").append(MathUtil.clamp(databaseField.size(), 0, 255)).append(")");
         } else {
            sb.append("(").append(databaseField.size()).append(")");
         }
      } else if (dataType.equals("VARCHAR")) {
         sb.append("(255)");
      }

      return sb.toString();
   }

   static {
      lookupMap.put(Integer.class, "INTEGER");
      lookupMap.put(Short.class, "TINYINT");
      lookupMap.put(Double.class, "REAL");
      lookupMap.put(Float.class, "REAL");
      lookupMap.put(Long.class, "BIGINT");
      lookupMap.put(String.class, "VARCHAR");
      lookupMap.put(Boolean.class, "BOOLEAN");
   }
}
