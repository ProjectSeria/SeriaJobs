package id.seria.gacha.lib.common.utils.plugin.migrations;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import lombok.Generated;
import org.bukkit.plugin.java.JavaPlugin;

public class MigrationsHandler {
   private final JavaPlugin plugin;
   private final Gson gson;

   public static void runMigrations(JavaPlugin plugin, List<MigrationsHandler.PluginMigration> migrations) {
      new MigrationsHandler(plugin).runMigrations(migrations);
   }

   public MigrationsHandler(JavaPlugin plugin) {
      this.plugin = plugin;
      this.gson = new Gson();
   }

   public void runMigrations(List<MigrationsHandler.PluginMigration> migrations) {
      long currentMigration = this.getCurrentMigrationFromFile();
      this.plugin.getLogger().info("Current migration: #" + currentMigration);
      boolean ignoreMigrations = currentMigration < 0L;
      boolean migrationsExecuted = false;

      for (MigrationsHandler.PluginMigration migration : migrations) {
         long migrationId = migration.getCurrentId();
         if (migrationId > currentMigration) {
            if (!ignoreMigrations) {
               try {
                  migration.accept(this.plugin);
                  migrationsExecuted = true;
                  this.plugin.getLogger().info("Migration #" + migrationId + " executed successfully.");
               } catch (Exception e) {
                  this.plugin.getLogger().severe("Error executing migration " + migrationId + ": " + e.getMessage());
               }
            }

            currentMigration = migrationId;
         }
      }

      if (!migrationsExecuted) {
         this.plugin.getLogger().info("No migrations to run.");
      }

      this.saveCurrentMigrationToFile(currentMigration);
   }

   private long getCurrentMigrationFromFile() {
      File configFile = new File(this.plugin.getDataFolder(), "migrations.json");
      if (!configFile.exists()) {
         this.plugin.getLogger().info("Migrations file not founded.");
         return -1L;
      }

      try {
         FileReader reader = new FileReader(configFile);

         label36: {
            long var4;
            try {
               JsonObject jsonObject = (JsonObject)this.gson.fromJson(reader, JsonObject.class);
               if (jsonObject == null || !jsonObject.has("currentMigration")) {
                  break label36;
               }

               var4 = jsonObject.get("currentMigration").getAsLong();
            } catch (Throwable var7) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            reader.close();
            return var4;
         }

         reader.close();
      } catch (IOException e) {
         e.printStackTrace();
      }

      return 0L;
   }

   private void saveCurrentMigrationToFile(long currentMigration) {
      File configFile = new File(this.plugin.getDataFolder(), "migrations.json");
      if (!configFile.exists()) {
         try {
            configFile.createNewFile();
         } catch (IOException e) {
            e.printStackTrace();
            return;
         }
      }

      try {
         FileWriter writer = new FileWriter(configFile);

         try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("currentMigration", currentMigration);
            this.gson.toJson(jsonObject, writer);
         } catch (Throwable var8) {
            try {
               writer.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         writer.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Deprecated
   public static class LegacyMigration<T extends JavaPlugin> extends MigrationsHandler.PluginMigration<T> {
      private MigrationsHandler.MigrationConsumer<T> action;

      public LegacyMigration(long currentId, MigrationsHandler.MigrationConsumer<T> action) {
         super(currentId);
         this.action = action;
      }

      @Override
      public void accept(T plugin) throws Exception {
         this.action.accept(plugin);
      }

      @Generated
      public void setAction(MigrationsHandler.MigrationConsumer<T> action) {
         this.action = action;
      }

      @Generated
      public MigrationsHandler.MigrationConsumer<T> getAction() {
         return this.action;
      }
   }

   public interface MigrationConsumer<T> {
      void accept(T var1) throws Exception;
   }

   public abstract static class PluginMigration<T extends JavaPlugin> {
      private final long currentId;

      public abstract void accept(T var1) throws Exception;

      @Generated
      public long getCurrentId() {
         return this.currentId;
      }

      @Generated
      public PluginMigration(long currentId) {
         this.currentId = currentId;
      }
   }
}
