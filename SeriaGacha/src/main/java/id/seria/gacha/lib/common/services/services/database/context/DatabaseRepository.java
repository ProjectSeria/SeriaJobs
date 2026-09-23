package id.seria.gacha.lib.common.services.services.database.context;

import com.google.common.collect.Lists;
import com.google.gson.internal.Primitives;
import id.seria.gacha.api.sdk.database.DbModel;
import id.seria.gacha.lib.common.services.services.database.DatabaseORM;
import id.seria.gacha.lib.common.services.services.database.DatabaseProvider;
import id.seria.gacha.lib.common.services.services.database.DatabaseType;
import id.seria.gacha.lib.common.services.services.database.context.annotations.DatabaseField;
import id.seria.gacha.lib.common.services.services.database.context.annotations.DatabaseTable;
import id.seria.gacha.lib.common.services.services.database.context.annotations.keys.PrimaryKey;
import id.seria.gacha.lib.common.services.services.database.schema.SchemaColumn;
import id.seria.gacha.lib.common.utils.Utilities;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;

public class DatabaseRepository<T extends DbModel> {
   private static final Map<Class<?>, List<DatabaseRepository.RepositoryLifecycleHook<?>>> LIFECYCLE_HOOKS = new ConcurrentHashMap<>();
   public static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(
      Utilities.getPluginInstance().getConfiguration().getDatabase().getMaximumPoolSize(), new ThreadFactory() {
         private final AtomicInteger counter = new AtomicInteger(1);
         private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

         @Override
         public Thread newThread(@NotNull Runnable r) {
            Thread t = this.defaultFactory.newThread(r);
            t.setName(Utilities.getPluginInstance().getName() + "-Db-" + this.counter.getAndIncrement());
            return t;
         }
      }
   );
   protected final DatabaseProvider provider;
   private final Class<T> modelClass;
   private final DatabaseTable tableAnnotation;
   protected final String currentMigration;

   public static <M extends DbModel> void registerLifecycleHook(Class<M> modelClass, DatabaseRepository.RepositoryLifecycleHook<M> hook) {
      Validate.notNull(modelClass, "modelClass");
      Validate.notNull(hook, "hook");
      LIFECYCLE_HOOKS.computeIfAbsent(modelClass, key -> new CopyOnWriteArrayList<>()).add(hook);
   }

   public DatabaseRepository(DatabaseProvider provider, Class<T> modelClass) {
      this.provider = provider;
      this.modelClass = modelClass;
      this.currentMigration = "17255317";
      if (!modelClass.isAnnotationPresent(DatabaseTable.class)) {
         throw new IllegalArgumentException("Cannot create a repository query from a not-table class.");
      }

      this.tableAnnotation = modelClass.getAnnotation(DatabaseTable.class);
      boolean tableExistedBeforeSetup = this.tableExists();
      this.executeLifecycleHooks(DatabaseRepository.RepositoryLifecyclePhase.BEFORE_TABLE_SETUP, tableExistedBeforeSetup, false);

      try {
         Connection connection = this.getProvider().getDataSource().getConnection();

         try {
            Statement stmt = connection.createStatement();

            try {
               String query = this.provider.getType() == DatabaseType.SQLITE
                  ? DatabaseORM.createSqliteTableQuery(provider, modelClass)
                  : DatabaseORM.createMysqlTableQuery(provider, modelClass);
               stmt.execute(query);
            } catch (Throwable var10) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var11) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var8) {
                  var11.addSuppressed(var8);
               }
            }

            throw var11;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      Map<String, SchemaColumn> databaseTableSchema = this.fetchTableSchema();
      boolean schemaMigrated = false;
      if (this.isNeedingMigration(databaseTableSchema)) {
         schemaMigrated = true;
         if (this.provider.getType() == DatabaseType.SQLITE) {
            this.migrateAsSqlite(databaseTableSchema);
         } else {
            this.migrateAsNonSqlite(databaseTableSchema);
         }
      }

      this.executeLifecycleHooks(DatabaseRepository.RepositoryLifecyclePhase.AFTER_SCHEMA_SETUP, tableExistedBeforeSetup, schemaMigrated);
   }

   private void executeLifecycleHooks(DatabaseRepository.RepositoryLifecyclePhase phase, boolean tableExistedBeforeSetup, boolean schemaMigrated) {
      List<DatabaseRepository.RepositoryLifecycleHook<?>> hooks = LIFECYCLE_HOOKS.get(this.modelClass);
      if (hooks != null && !hooks.isEmpty()) {
         DatabaseRepository.RepositoryLifecycleContext<T> context = new DatabaseRepository.RepositoryLifecycleContext<>(
            this, phase, tableExistedBeforeSetup, schemaMigrated
         );

         for (DatabaseRepository.RepositoryLifecycleHook<?> rawHook : hooks) {
            try {
               DatabaseRepository.RepositoryLifecycleHook<T> hook = (DatabaseRepository.RepositoryLifecycleHook<T>)rawHook;
               hook.handle(context);
            } catch (Exception ex) {
               ex.printStackTrace();
            }
         }
      }
   }

   private boolean tableExists() {
      String tableName = this.provider.formatTableName(this.tableAnnotation);

      try {
         Connection connection = this.provider.getDataSource().getConnection();

         boolean var15;
         label141: {
            boolean var6;
            label142: {
               boolean existing;
               label152: {
                  try {
                     DatabaseMetaData metaData = connection.getMetaData();
                     ResultSet directMatch = metaData.getTables(null, null, tableName, new String[]{"TABLE"});

                     label144: {
                        try {
                           if (!directMatch.next()) {
                              break label144;
                           }

                           existing = true;
                        } catch (Throwable var10) {
                           if (directMatch != null) {
                              try {
                                 directMatch.close();
                              } catch (Throwable var9) {
                                 var10.addSuppressed(var9);
                              }
                           }

                           throw var10;
                        }

                        if (directMatch != null) {
                           directMatch.close();
                        }
                        break label152;
                     }

                     if (directMatch != null) {
                        directMatch.close();
                     }

                     ResultSet allTables = metaData.getTables(null, null, null, new String[]{"TABLE"});

                     label145: {
                        try {
                           while (true) {
                              if (!allTables.next()) {
                                 break label145;
                              }

                              String existingx = allTables.getString("TABLE_NAME");
                              if (existingx != null && existingx.equalsIgnoreCase(tableName)) {
                                 var6 = true;
                                 break;
                              }
                           }
                        } catch (Throwable var11) {
                           if (allTables != null) {
                              try {
                                 allTables.close();
                              } catch (Throwable var8) {
                                 var11.addSuppressed(var8);
                              }
                           }

                           throw var11;
                        }

                        if (allTables != null) {
                           allTables.close();
                        }
                        break label142;
                     }

                     if (allTables != null) {
                        allTables.close();
                     }

                     if (this.provider.getType() == DatabaseType.SQLITE) {
                        var15 = this.tableExistsSqliteFallback(connection, tableName);
                        break label141;
                     }
                  } catch (Throwable var12) {
                     if (connection != null) {
                        try {
                           connection.close();
                        } catch (Throwable var7) {
                           var12.addSuppressed(var7);
                        }
                     }

                     throw var12;
                  }

                  if (connection != null) {
                     connection.close();
                  }

                  return false;
               }

               if (connection != null) {
                  connection.close();
               }

               return existing;
            }

            if (connection != null) {
               connection.close();
            }

            return var6;
         }

         if (connection != null) {
            connection.close();
         }

         return var15;
      } catch (Exception ex) {
         ex.printStackTrace();
         return false;
      }
   }

   private boolean tableExistsSqliteFallback(Connection connection, String tableName) {
      String query = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND lower(name) = lower(?) LIMIT 1";

      try {
         PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM sqlite_master WHERE type = 'table' AND lower(name) = lower(?) LIMIT 1");

         boolean var6;
         label83: {
            try {
               statement.setString(1, tableName);
               ResultSet resultSet = statement.executeQuery();

               label78: {
                  try {
                     if (!resultSet.next()) {
                        break label78;
                     }

                     var6 = true;
                  } catch (Throwable var10) {
                     if (resultSet != null) {
                        try {
                           resultSet.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
                        }
                     }

                     throw var10;
                  }

                  if (resultSet != null) {
                     resultSet.close();
                  }
                  break label83;
               }

               if (resultSet != null) {
                  resultSet.close();
               }
            } catch (Throwable var11) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (statement != null) {
               statement.close();
            }

            return false;
         }

         if (statement != null) {
            statement.close();
         }

         return var6;
      } catch (Exception ex) {
         ex.printStackTrace();
         return false;
      }
   }

   private void migrateAsNonSqlite(Map<String, SchemaColumn> databaseTableSchema) {
      Validate.isTrue(this.provider.getType() != DatabaseType.SQLITE, "Expected Mysql or MariaDB database");

      try {
         Connection connection = null;

         try {
            connection = this.getProvider().getDataSource().getConnection();
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();

            try {
               for (Field field : this.modelClass.getDeclaredFields()) {
                  if (field.isAnnotationPresent(DatabaseField.class)) {
                     DatabaseField fieldAnnotation = field.getAnnotation(DatabaseField.class);
                     String databaseColumnName = WordUtils.capitalize(field.getName());
                     SchemaColumn columnSchema = databaseTableSchema.get(databaseColumnName);
                     if (columnSchema != null) {
                        if (!columnSchema.getType().equalsIgnoreCase(DatabaseORM.lookupMap.get(Primitives.wrap(field.getType())))
                           || columnSchema.getSize() != fieldAnnotation.size() && fieldAnnotation.size() > 0) {
                           stmt.execute(DatabaseORM.createMysqlModifyColumnQuery(this.provider, this.modelClass, field));
                        }
                     } else {
                        boolean columnExists = false;

                        for (String existingColumnName : databaseTableSchema.keySet()) {
                           if (existingColumnName.equalsIgnoreCase(databaseColumnName)) {
                              columnExists = true;
                              break;
                           }
                        }

                        if (!columnExists) {
                           stmt.execute(DatabaseORM.createAddColumnQuery(this.provider, this.modelClass, field));
                        }
                     }
                  }
               }
            } catch (Throwable var21) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var20) {
                     var21.addSuppressed(var20);
                  }
               }

               throw var21;
            }

            if (stmt != null) {
               stmt.close();
            }

            connection.commit();
         } catch (Exception ex) {
            if (connection != null) {
               connection.rollback();
            }

            ex.printStackTrace();
         } finally {
            if (connection != null) {
               connection.setAutoCommit(true);
               connection.close();
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void migrateAsSqlite(Map<String, SchemaColumn> databaseTableSchema) {
      Validate.isTrue(this.provider.getType() == DatabaseType.SQLITE, "Expected Sqlite database");

      try {
         Connection connection = null;

         try {
            connection = this.getProvider().getDataSource().getConnection();
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();

            try {
               stmt.execute(
                  "ALTER TABLE `"
                     + this.provider.formatTableName(this.tableAnnotation)
                     + "` RENAME TO `temp_"
                     + this.provider.formatTableName(this.tableAnnotation)
                     + "`;"
               );
               Map<String, SchemaColumn> legacyColumns = new LinkedHashMap<>();

               for (Entry<String, SchemaColumn> entry : databaseTableSchema.entrySet()) {
                  boolean isModelField = false;

                  for (Field f : this.modelClass.getDeclaredFields()) {
                     if (f.isAnnotationPresent(DatabaseField.class) && WordUtils.capitalize(f.getName()).equalsIgnoreCase(entry.getKey())) {
                        isModelField = true;
                        break;
                     }
                  }

                  if (!isModelField) {
                     legacyColumns.put(entry.getKey(), entry.getValue());
                  }
               }

               stmt.execute(DatabaseORM.createSqliteTableQuery(this.provider, this.modelClass, legacyColumns));
               List<String> columns = new ArrayList<>();
               List<String> arguments = new ArrayList<>();

               for (Field field : this.modelClass.getDeclaredFields()) {
                  if (field.isAnnotationPresent(DatabaseField.class)) {
                     DatabaseField fieldAnnotation = field.getAnnotation(DatabaseField.class);
                     String databaseColumnName = WordUtils.capitalize(field.getName());
                     SchemaColumn columnSchema = databaseTableSchema.get(databaseColumnName);
                     if (columnSchema != null) {
                        columns.add(databaseColumnName);
                        if (columnSchema.getType().equalsIgnoreCase(DatabaseORM.lookupMap.get(Primitives.wrap(field.getType())))
                           && (columnSchema.getSize() == fieldAnnotation.size() || fieldAnnotation.size() <= 0)) {
                           arguments.add(databaseColumnName);
                        } else {
                           arguments.add(String.format("cast(%s as %s)", databaseColumnName, DatabaseORM.getSqlDataType(this.provider, field)));
                        }
                     }
                  }
               }

               for (String legacyColName : legacyColumns.keySet()) {
                  columns.add(legacyColName);
                  arguments.add(legacyColName);
               }

               stmt.execute(
                  String.format(
                     "INSERT INTO `%s` (%s) SELECT %s FROM `%s`;",
                     this.provider.formatTableName(this.tableAnnotation),
                     String.join(", ", columns),
                     String.join(", ", arguments),
                     "temp_" + this.provider.formatTableName(this.tableAnnotation)
                  )
               );
               stmt.execute(String.format("DROP TABLE `%s`;", "temp_" + this.provider.formatTableName(this.tableAnnotation)));
            } catch (Throwable var21) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var20) {
                     var21.addSuppressed(var20);
                  }
               }

               throw var21;
            }

            if (stmt != null) {
               stmt.close();
            }

            connection.commit();
         } catch (Exception ex) {
            if (connection != null) {
               connection.rollback();
            }

            ex.printStackTrace();
         } finally {
            if (connection != null) {
               connection.setAutoCommit(true);
               connection.close();
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private Map<String, SchemaColumn> fetchTableSchema() {
      Map<String, SchemaColumn> columns = new HashMap<>();
      if (this.provider.getType() == DatabaseType.SQLITE) {
         try {
            Connection connection = this.provider.getDataSource().getConnection();

            try {
               DatabaseMetaData meta = connection.getMetaData();
               ResultSet rs = meta.getColumns(null, null, this.provider.formatTableName(this.tableAnnotation), null);
               ResultSet rs2 = connection.createStatement().executeQuery("PRAGMA table_info(" + this.provider.formatTableName(this.tableAnnotation) + ");");

               while (rs.next()) {
                  String columnName = rs.getString("COLUMN_NAME");
                  String dataType = rs.getString("TYPE_NAME");
                  if (dataType.contains("(")) {
                     dataType = dataType.substring(0, dataType.indexOf("("));
                  }

                  int columnSize = rs.getInt("COLUMN_SIZE");
                  if (!dataType.equals("INTEGER") && !dataType.equals("INT")) {
                     if (columnSize == 2000000000) {
                        while (rs2.next()) {
                           if (rs2.getString("name").equals(columnName)) {
                              String type = rs2.getString("type");
                              int startIndex = type.indexOf("(");
                              int endIndex = type.indexOf(")");
                              if (startIndex != -1 && endIndex != -1) {
                                 columnSize = Integer.parseInt(type.substring(startIndex + 1, endIndex));
                              }
                              break;
                           }
                        }
                     }
                  } else {
                     columnSize = -1;
                  }

                  columns.put(columnName, new SchemaColumn(columnName, dataType, columnSize));
               }

               connection.close();
            } catch (Throwable var16) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var13) {
                     var16.addSuppressed(var13);
                  }
               }

               throw var16;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            e.printStackTrace();
         }
      } else {
         try {
            Connection connection = this.provider.getDataSource().getConnection();

            try {
               DatabaseMetaData meta = connection.getMetaData();
               ResultSet rs = meta.getColumns(null, null, this.provider.formatTableName(this.tableAnnotation), null);

               while (rs.next()) {
                  String columnName = rs.getString("COLUMN_NAME");
                  String normalizedColumnName = WordUtils.capitalize(columnName);
                  String columnType = rs.getString("TYPE_NAME");
                  if (columnType.equalsIgnoreCase("INT")) {
                     columnType = "INTEGER";
                  }

                  columns.put(normalizedColumnName, new SchemaColumn(normalizedColumnName, columnType, rs.getInt("COLUMN_SIZE")));
               }

               connection.close();
            } catch (Throwable var14) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var12) {
                     var14.addSuppressed(var12);
                  }
               }

               throw var14;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            e.printStackTrace();
         }
      }

      return columns;
   }

   private boolean isNeedingMigration(Map<String, SchemaColumn> databaseTableSchema) {
      for (Field field : this.modelClass.getDeclaredFields()) {
         if (field.isAnnotationPresent(DatabaseField.class)) {
            DatabaseField fieldAnnotation = field.getAnnotation(DatabaseField.class);
            SchemaColumn columnSchema = databaseTableSchema.get(WordUtils.capitalize(field.getName()));
            if (columnSchema == null
               || !columnSchema.getType().equalsIgnoreCase(DatabaseORM.lookupMap.get(Primitives.wrap(field.getType())))
               || columnSchema.getSize() != fieldAnnotation.size() && fieldAnnotation.size() > 0) {
               return true;
            }
         }
      }

      return false;
   }

   public String getTableName() {
      return this.provider.formatTableName(this.modelClass.getAnnotation(DatabaseTable.class));
   }

   public T fetchNowBy(String key, Object value) {
      key = WordUtils.capitalize(key);

      try {
         Connection connection = this.getProvider().getDataSource().getConnection();

         DbModel var6;
         label116: {
            try {
               PreparedStatement stmt;
               label108: {
                  stmt = connection.prepareStatement(String.format("SELECT * FROM `%s` WHERE `%s` = ? LIMIT ?", this.getTableName(), key));

                  try {
                     stmt.setObject(1, value);
                     stmt.setInt(2, 1);
                     ResultSet rs = stmt.executeQuery();

                     label110: {
                        try {
                           if (rs.next()) {
                              var6 = this.read(rs);
                              break label110;
                           }
                        } catch (Throwable var11) {
                           if (rs != null) {
                              try {
                                 rs.close();
                              } catch (Throwable var10) {
                                 var11.addSuppressed(var10);
                              }
                           }

                           throw var11;
                        }

                        if (rs != null) {
                           rs.close();
                        }

                        connection.close();
                        break label108;
                     }

                     if (rs != null) {
                        rs.close();
                     }
                  } catch (Throwable var12) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable var9) {
                           var12.addSuppressed(var9);
                        }
                     }

                     throw var12;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
                  break label116;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var13) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var8) {
                     var13.addSuppressed(var8);
                  }
               }

               throw var13;
            }

            if (connection != null) {
               connection.close();
            }

            return null;
         }

         if (connection != null) {
            connection.close();
         }

         return (T)var6;
      } catch (Exception ex) {
         ex.printStackTrace();
         return null;
      }
   }

   public T fetchNowBy(Object... data) {
      String[] keys = new String[0];
      Object[] values = new Object[0];
      if (data.length % 2 != 0) {
         throw new IllegalArgumentException("Data don't have matching sizes!");
      }

      int length = data.length / 2;
      keys = new String[length];
      values = new Object[length];

      for (int i = 0; i < data.length; i++) {
         Object object = data[i];
         if (i % 2 == 0) {
            keys[i / 2] = WordUtils.capitalize(object.toString());
         } else {
            values[i / 2] = object == null ? "null" : object;
         }
      }

      try {
         Connection connection = this.getProvider().getDataSource().getConnection();

         DbModel var7;
         label149: {
            try {
               PreparedStatement stmt;
               label139: {
                  stmt = connection.prepareStatement(DatabaseORM.createSelectQuery(this.provider, this.modelClass, keys));

                  try {
                     for (int i = 1; i <= values.length; i++) {
                        stmt.setObject(i, values[i - 1]);
                     }

                     ResultSet rs = stmt.executeQuery();

                     label141: {
                        try {
                           if (rs.next()) {
                              var7 = this.read(rs);
                              break label141;
                           }
                        } catch (Throwable var12) {
                           if (rs != null) {
                              try {
                                 rs.close();
                              } catch (Throwable var11) {
                                 var12.addSuppressed(var11);
                              }
                           }

                           throw var12;
                        }

                        if (rs != null) {
                           rs.close();
                        }

                        connection.close();
                        break label139;
                     }

                     if (rs != null) {
                        rs.close();
                     }
                  } catch (Throwable var13) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable var10) {
                           var13.addSuppressed(var10);
                        }
                     }

                     throw var13;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
                  break label149;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var14) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var9) {
                     var14.addSuppressed(var9);
                  }
               }

               throw var14;
            }

            if (connection != null) {
               connection.close();
            }

            return null;
         }

         if (connection != null) {
            connection.close();
         }

         return (T)var7;
      } catch (Exception ex) {
         ex.printStackTrace();
         return null;
      }
   }

   public List<T> fetchAllNowBy(Object... data) {
      List<T> models = Lists.newArrayList();
      String[] keys = new String[0];
      Object[] values = new Object[0];
      if (data.length % 2 != 0) {
         throw new IllegalArgumentException("Data don't have matching sizes!");
      }

      int length = data.length / 2;
      keys = new String[length];
      values = new Object[length];

      for (int i = 0; i < data.length; i++) {
         Object object = data[i];
         if (i % 2 == 0) {
            keys[i / 2] = WordUtils.capitalize(object.toString());
         } else {
            values[i / 2] = object == null ? "null" : object;
         }
      }

      try {
         Connection connection = this.getProvider().getDataSource().getConnection();

         try {
            PreparedStatement stmt = connection.prepareStatement(DatabaseORM.createSelectQuery(this.provider, this.modelClass, keys));

            try {
               for (int i = 1; i <= values.length; i++) {
                  stmt.setObject(i, values[i - 1]);
               }

               ResultSet rs = stmt.executeQuery();

               try {
                  while (rs.next()) {
                     models.add(this.read(rs));
                  }
               } catch (Throwable var13) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                     }
                  }

                  throw var13;
               }

               if (rs != null) {
                  rs.close();
               }

               connection.close();
            } catch (Throwable var14) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var11) {
                     var14.addSuppressed(var11);
                  }
               }

               throw var14;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var15) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var10) {
                  var15.addSuppressed(var10);
               }
            }

            throw var15;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      return models;
   }

   public List<T> fetchAllNow() {
      List<T> models = Lists.newArrayList();

      try {
         Connection connection = this.getProvider().getDataSource().getConnection();

         try {
            PreparedStatement stmt = connection.prepareStatement(String.format("SELECT * FROM `%s`", this.getTableName()));

            try {
               ResultSet rs = stmt.executeQuery();

               try {
                  while (rs.next()) {
                     models.add(this.read(rs));
                  }
               } catch (Throwable var10) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (rs != null) {
                  rs.close();
               }

               connection.close();
            } catch (Throwable var11) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var12) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var12.addSuppressed(var7);
               }
            }

            throw var12;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      return models;
   }

   public CompletableFuture<Boolean> insertLater(T model) {
      Exception originalException = new Exception();
      return CompletableFuture.supplyAsync(() -> {
         synchronized (model.getLock()) {
            Boolean var22;
            try {
               Connection connection = this.getProvider().getDataSource().getConnection();

               try {
                  PreparedStatement stmt = connection.prepareStatement(DatabaseORM.createInsertQuery(this.provider, this.modelClass), 1);

                  try {
                     int i = 1;

                     for (Field field : this.modelClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(DatabaseField.class) && !field.getAnnotation(DatabaseField.class).autoIncrement()) {
                           field.setAccessible(true);
                           stmt.setObject(i++, field.get(model));
                        }
                     }

                     int affectedRows = stmt.executeUpdate();
                     if (affectedRows == 0) {
                        throw new SQLException("Creating user failed, no rows affected.");
                     }

                     ResultSet generatedKeys = stmt.getGeneratedKeys();

                     try {
                        if (!generatedKeys.next()) {
                           throw new SQLException("Creating user failed, no ID obtained.");
                        }

                        if (model instanceof DbModel) {
                           model.setId(generatedKeys.getInt(1));
                        }

                        var22 = true;
                     } catch (Throwable t$) {
                        if (generatedKeys != null) {
                           try {
                              generatedKeys.close();
                           } catch (Throwable x2) {
                              t$.addSuppressed(x2);
                           }
                        }

                        throw t$;
                     }

                     if (generatedKeys != null) {
                        generatedKeys.close();
                     }
                  } catch (Throwable t$) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable x2) {
                           t$.addSuppressed(x2);
                        }
                     }

                     throw t$;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (Throwable t$) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable x2) {
                        t$.addSuppressed(x2);
                     }
                  }

                  throw t$;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (Exception ex) {
               ex.initCause(originalException);
               ex.printStackTrace();
               return false;
            }

            return var22;
         }
      }, THREAD_POOL);
   }

   public CompletableFuture<Boolean> insertNow(T model) throws SQLException, IllegalAccessException {
      Exception originalException = new Exception();
      synchronized (model.getLock()) {
         CompletableFuture var22;
         try {
            Connection connection = this.getProvider().getDataSource().getConnection();

            try {
               PreparedStatement stmt = connection.prepareStatement(DatabaseORM.createInsertQuery(this.provider, this.modelClass), 1);

               try {
                  int i = 1;

                  for (Field field : this.modelClass.getDeclaredFields()) {
                     if (field.isAnnotationPresent(DatabaseField.class) && !field.getAnnotation(DatabaseField.class).autoIncrement()) {
                        field.setAccessible(true);
                        stmt.setObject(i++, field.get(model));
                     }
                  }

                  int affectedRows = stmt.executeUpdate();
                  if (affectedRows == 0) {
                     throw new SQLException("Creating user failed, no rows affected.");
                  }

                  ResultSet generatedKeys = stmt.getGeneratedKeys();

                  try {
                     if (!generatedKeys.next()) {
                        throw new SQLException("Creating user failed, no ID obtained.");
                     }

                     if (model instanceof DbModel) {
                        model.setId(generatedKeys.getInt(1));
                     }

                     var22 = CompletableFuture.completedFuture(true);
                  } catch (Throwable var15) {
                     if (generatedKeys != null) {
                        try {
                           generatedKeys.close();
                        } catch (Throwable var14) {
                           var15.addSuppressed(var14);
                        }
                     }

                     throw var15;
                  }

                  if (generatedKeys != null) {
                     generatedKeys.close();
                  }
               } catch (Throwable var16) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var13) {
                        var16.addSuppressed(var13);
                     }
                  }

                  throw var16;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var17) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var12) {
                     var17.addSuppressed(var12);
                  }
               }

               throw var17;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (Exception ex) {
            ex.initCause(originalException);
            throw ex;
         }

         return var22;
      }
   }

   public CompletableFuture<Boolean> updateNow(T model) {
      Exception originalException = new Exception();
      boolean result = false;
      synchronized (model.getLock()) {
         try {
            Connection connection = this.getProvider().getDataSource().getConnection();

            try {
               PreparedStatement stmt = connection.prepareStatement(DatabaseORM.createUpdateQuery(this.provider, this.modelClass));

               try {
                  int i = 1;

                  for (Field field : this.modelClass.getDeclaredFields()) {
                     if (field.isAnnotationPresent(DatabaseField.class) && !field.getAnnotation(DatabaseField.class).autoIncrement()) {
                        field.setAccessible(true);
                        stmt.setObject(i++, field.get(model));
                     }
                  }

                  for (Field field : this.modelClass.getDeclaredFields()) {
                     if (field.isAnnotationPresent(PrimaryKey.class)) {
                        field.setAccessible(true);
                        stmt.setObject(i++, field.get(model));
                     }
                  }

                  result = stmt.executeUpdate() != 0;
               } catch (Throwable var15) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var14) {
                        var15.addSuppressed(var14);
                     }
                  }

                  throw var15;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var16) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var13) {
                     var16.addSuppressed(var13);
                  }
               }

               throw var16;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (Exception ex) {
            ex.initCause(originalException);
            ex.printStackTrace();
         }
      }

      return CompletableFuture.completedFuture(result);
   }

   public CompletableFuture<Boolean> updateLater(T model) {
      Exception originalException = new Exception();
      return CompletableFuture.supplyAsync(() -> {
         synchronized (model.getLock()) {
            Boolean var19;
            try {
               Connection connection = this.getProvider().getDataSource().getConnection();

               try {
                  PreparedStatement stmt = connection.prepareStatement(DatabaseORM.createUpdateQuery(this.provider, this.modelClass));

                  try {
                     int i = 1;

                     for (Field field : this.modelClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(DatabaseField.class) && !field.getAnnotation(DatabaseField.class).autoIncrement()) {
                           field.setAccessible(true);
                           stmt.setObject(i++, field.get(model));
                        }
                     }

                     for (Field field : this.modelClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(PrimaryKey.class)) {
                           field.setAccessible(true);
                           stmt.setObject(i++, field.get(model));
                        }
                     }

                     var19 = stmt.executeUpdate() != 0;
                  } catch (Throwable t$) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable x2) {
                           t$.addSuppressed(x2);
                        }
                     }

                     throw t$;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (Throwable t$) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable x2) {
                        t$.addSuppressed(x2);
                     }
                  }

                  throw t$;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (Exception ex) {
               ex.initCause(originalException);
               ex.printStackTrace();
               return false;
            }

            return var19;
         }
      }, THREAD_POOL);
   }

   public CompletableFuture<Void> updateAllLater(List<T> models) {
      Exception originalException = new Exception();
      return CompletableFuture.supplyAsync(() -> {
         try {
            Connection connection = this.getProvider().getDataSource().getConnection();

            try {
               connection.setAutoCommit(false);

               for (T model : models) {
                  synchronized (model.getLock()) {
                     try {
                        PreparedStatement stmt = connection.prepareStatement(DatabaseORM.createUpdateQuery(this.provider, this.modelClass));

                        try {
                           int i = 1;

                           for (Field field : this.modelClass.getDeclaredFields()) {
                              if (field.isAnnotationPresent(DatabaseField.class) && !field.getAnnotation(DatabaseField.class).autoIncrement()) {
                                 field.setAccessible(true);
                                 stmt.setObject(i++, field.get(model));
                              }
                           }

                           for (Field field : this.modelClass.getDeclaredFields()) {
                              if (field.isAnnotationPresent(PrimaryKey.class)) {
                                 field.setAccessible(true);
                                 stmt.setObject(i++, field.get(model));
                              }
                           }

                           stmt.executeUpdate();
                        } catch (Throwable t$) {
                           if (stmt != null) {
                              try {
                                 stmt.close();
                              } catch (Throwable x2) {
                                 t$.addSuppressed(x2);
                              }
                           }

                           throw t$;
                        }

                        if (stmt != null) {
                           stmt.close();
                        }
                     } catch (Exception ex) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        throw ex;
                     }
                  }
               }

               connection.commit();
               connection.setAutoCommit(true);
            } catch (Throwable t$) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable x2) {
                     t$.addSuppressed(x2);
                  }
               }

               throw t$;
            }

            if (connection != null) {
               connection.close();
            }

            return null;
         } catch (Exception ex) {
            RuntimeException toThrow = new RuntimeException("Error updating all models", ex);
            toThrow.initCause(originalException);
            throw toThrow;
         }
      }, THREAD_POOL);
   }

   public CompletableFuture<Boolean> deleteLater(T model) {
      Exception originalException = new Exception();
      return CompletableFuture.supplyAsync(() -> {
         synchronized (model.getLock()) {
            Boolean var19;
            try {
               Connection connection = this.getProvider().getDataSource().getConnection();

               try {
                  PreparedStatement stmt = connection.prepareStatement(DatabaseORM.createDeleteQuery(this.provider, this.modelClass));

                  try {
                     int i = 1;

                     for (Field field : this.modelClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(PrimaryKey.class)) {
                           field.setAccessible(true);
                           stmt.setObject(i++, field.get(model));
                        }
                     }

                     boolean success = stmt.executeUpdate() != 0;
                     if (success) {
                        model.setId(-1);
                     }

                     var19 = success;
                  } catch (Throwable t$) {
                     if (stmt != null) {
                        try {
                           stmt.close();
                        } catch (Throwable x2) {
                           t$.addSuppressed(x2);
                        }
                     }

                     throw t$;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (Throwable t$) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable x2) {
                        t$.addSuppressed(x2);
                     }
                  }

                  throw t$;
               }

               if (connection != null) {
                  connection.close();
               }
            } catch (Exception ex) {
               ex.initCause(originalException);
               ex.printStackTrace();
               return false;
            }

            return var19;
         }
      }, THREAD_POOL);
   }

   public boolean deleteBy(String key, Object value) {
      try {
         Connection connection = this.getProvider().getDataSource().getConnection();

         boolean var5;
         try {
            PreparedStatement stmt = connection.prepareStatement(String.format("DELETE FROM `%s` WHERE `%s` = ?", this.getTableName(), key));

            try {
               stmt.setObject(1, value);
               var5 = stmt.executeUpdate() != 0;
            } catch (Throwable var9) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var10) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var7) {
                  var10.addSuppressed(var7);
               }
            }

            throw var10;
         }

         if (connection != null) {
            connection.close();
         }

         return var5;
      } catch (Exception ex) {
         ex.printStackTrace();
         return false;
      }
   }

   private T read(ResultSet result) throws Exception {
      T object = this.modelClass.getDeclaredConstructor().newInstance();

      for (Field field : this.modelClass.getDeclaredFields()) {
         if (field.isAnnotationPresent(DatabaseField.class)) {
            field.setAccessible(true);
            Class<?> fieldType = Primitives.wrap(field.getType());
            String name = WordUtils.capitalize(field.getName());
            Object value = result.getObject(name);
            if (Boolean.class.isAssignableFrom(fieldType)) {
               if (value instanceof Integer) {
                  field.set(object, (Integer)value == 1);
               } else {
                  field.set(object, value == null ? Boolean.FALSE : Boolean.valueOf(value.toString()));
               }
            } else {
               field.set(object, this.safeValue(fieldType, value));
            }
         }
      }

      return object;
   }

   private Object safeValue(Class<?> clazz, Object value) {
      if (!clazz.equals(int.class)
         && !clazz.equals(Integer.class)
         && !clazz.equals(double.class)
         && !clazz.equals(Double.class)
         && !clazz.equals(long.class)
         && !clazz.equals(Long.class)) {
         return value;
      } else {
         return value == null ? 0 : value;
      }
   }

   @Generated
   public DatabaseProvider getProvider() {
      return this.provider;
   }

   @Generated
   public Class<T> getModelClass() {
      return this.modelClass;
   }

   @Generated
   public DatabaseTable getTableAnnotation() {
      return this.tableAnnotation;
   }

   @Generated
   public String getCurrentMigration() {
      return this.currentMigration;
   }

   static {
      Validate.isTrue(Utilities.getPluginInstance() != null, "Utilities not initialized");
   }

   public static final class RepositoryLifecycleContext<M extends DbModel> {
      private final DatabaseRepository<M> repository;
      private final DatabaseRepository.RepositoryLifecyclePhase phase;
      private final boolean tableExistedBeforeSetup;
      private final boolean schemaMigrated;

      private RepositoryLifecycleContext(
         DatabaseRepository<M> repository, DatabaseRepository.RepositoryLifecyclePhase phase, boolean tableExistedBeforeSetup, boolean schemaMigrated
      ) {
         this.repository = repository;
         this.phase = phase;
         this.tableExistedBeforeSetup = tableExistedBeforeSetup;
         this.schemaMigrated = schemaMigrated;
      }

      public DatabaseRepository<M> getRepository() {
         return this.repository;
      }

      public DatabaseRepository.RepositoryLifecyclePhase getPhase() {
         return this.phase;
      }

      public boolean isTableExistedBeforeSetup() {
         return this.tableExistedBeforeSetup;
      }

      public boolean isSchemaMigrated() {
         return this.schemaMigrated;
      }
   }

   public interface RepositoryLifecycleHook<M extends DbModel> {
      void handle(DatabaseRepository.RepositoryLifecycleContext<M> var1) throws Exception;
   }

   public enum RepositoryLifecyclePhase {
      BEFORE_TABLE_SETUP,
      AFTER_SCHEMA_SETUP;

      RepositoryLifecyclePhase() {
      }
   }
}
