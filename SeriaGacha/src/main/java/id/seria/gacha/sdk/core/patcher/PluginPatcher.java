package id.seria.gacha.sdk.core.patcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import id.seria.gacha.api.sdk.plugin.Plugin;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.NonNull;

public abstract class PluginPatcher<T extends Plugin> {
   private static final Gson GSON = new GsonBuilder().create();
   private final List<PluginPatcher.Patch> toLoadPatches = new ArrayList<>();
   protected final T plugin;

   public PluginPatcher(T plugin) {
      this.plugin = plugin;
   }

   public void addPatch(PluginPatcher.Patch patch) {
      synchronized (this.toLoadPatches) {
         this.toLoadPatches.add(patch);
      }
   }

   @Deprecated
   public void addPatch(long id, @NonNull final PluginPatcher.PatchRunner runnable) {
      if (runnable == null) {
         throw new NullPointerException("runnable is marked non-null but is null");
      }

      synchronized (this.toLoadPatches) {
         this.toLoadPatches.add(new PluginPatcher.Patch(String.valueOf(id)) {
            @Override
            public void run(Plugin ignored) throws Exception {
               runnable.run();
            }
         });
      }
   }

   @Deprecated
   public void addPatch(String date, @NonNull final PluginPatcher.PatchRunner runnable) {
      if (runnable == null) {
         throw new NullPointerException("runnable is marked non-null but is null");
      }

      synchronized (this.toLoadPatches) {
         this.toLoadPatches.add(new PluginPatcher.Patch(date) {
            @Override
            public void run(Plugin ignored) throws Exception {
               runnable.run();
            }
         });
      }
   }

   public abstract void onLoad();

   public void load() {
      synchronized (this.toLoadPatches) {
         this.toLoadPatches.clear();
         this.onLoad();
         this.runPatches();
      }
   }

   private void runPatches() {
      Map<Long, PluginPatcher.Patch> patchMap = this.toLoadPatches
         .stream()
         .sorted(Comparator.comparingLong(patchx -> toId(patchx.getCurrentDate())))
         .collect(Collectors.toMap(patchx -> toId(patchx.getCurrentDate()), patchx -> patchx, (existing, replacement) -> replacement, LinkedHashMap::new));
      long currentPatch = this.getCurrentPatchFromFile();
      if (currentPatch < 0L) {
         this.plugin.getLogger().info("No patches to run.");
         OptionalLong latestId = patchMap.keySet().stream().mapToLong(Long::longValue).max();
         if (latestId.isPresent()) {
            this.saveCurrentPatchToFile(latestId.getAsLong());
         } else {
            this.saveCurrentPatchToFile(0L);
         }
      } else {
         this.plugin.getLogger().info("Current patch: #" + currentPatch);
         long finalCurrentPatch = currentPatch;
         List<Entry<Long, PluginPatcher.Patch>> patchesToRun = patchMap.entrySet()
            .stream()
            .filter(entryx -> (Long)entryx.getKey() > finalCurrentPatch)
            .collect(Collectors.toList());
         if (patchesToRun.isEmpty()) {
            this.plugin.getLogger().info("No patches to run.");
         } else {
            this.plugin.getLogger().info("Founded some patches to run. Preparing a backup.");
            long start = System.currentTimeMillis();
            PluginBackup.createBackup(this.plugin);
            long elapsed = System.currentTimeMillis() - start;
            this.plugin.getLogger().info("Backup completed. (" + elapsed + " ms)");

            for (Entry<Long, PluginPatcher.Patch> entry : patchesToRun) {
               long patchId = entry.getKey();
               PluginPatcher.Patch patch = entry.getValue();

               try {
                  patch.run(this.plugin);
                  this.plugin.getLogger().info("Patch #" + patchId + " executed successfully.");
               } catch (Exception e) {
                  this.plugin.getLogger().error("Error executing patch " + patchId, e);
               }

               currentPatch = patchId;
            }

            this.saveCurrentPatchToFile(currentPatch);
         }
      }
   }

   public static long toId(String date) {
      if (date.matches("\\d+")) {
         return Long.parseLong(date);
      }

      String[] parts = date.split("[-:]");
      String[] dateParts = parts[0].split("\\.");
      String year = dateParts.length > 0 ? dateParts[0] : "0000";
      String month = dateParts.length > 1 ? String.format("%02d", Integer.parseInt(dateParts[1])) : "00";
      String day = dateParts.length > 2 ? String.format("%02d", Integer.parseInt(dateParts[2])) : "00";
      String hour = parts.length > 1 ? String.format("%02d", Integer.parseInt(parts[1])) : "00";
      String minute = parts.length > 2 ? String.format("%02d", Integer.parseInt(parts[2])) : "00";
      return Long.parseLong(year + month + day + hour + minute);
   }

   public long getCurrentPatchFromFile() {
      File patchsFile = new File(this.plugin.getDataFolder(), "patchs.json");
      File legacyFile = new File(this.plugin.getDataFolder(), "migrations.json");
      if (patchsFile.exists()) {
         try {
            FileReader reader = new FileReader(patchsFile);

            long var15;
            label94: {
               try {
                  JsonObject jsonObject = (JsonObject)GSON.fromJson(reader, JsonObject.class);
                  if (jsonObject != null && jsonObject.has("currentPatch")) {
                     var15 = jsonObject.get("currentPatch").getAsLong();
                     break label94;
                  }
               } catch (Throwable var9) {
                  try {
                     reader.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }

                  throw var9;
               }

               reader.close();
               return 0L;
            }

            reader.close();
            return var15;
         } catch (IOException e) {
            this.plugin.getLogger().error("Error reading patchs.json file.", e);
         }
      } else {
         if (!legacyFile.exists()) {
            this.plugin.getLogger().info("Patch files not found.");
            return -1L;
         }

         try {
            FileReader reader = new FileReader(legacyFile);

            long var5;
            label95: {
               try {
                  JsonObject jsonObject = (JsonObject)GSON.fromJson(reader, JsonObject.class);
                  if (jsonObject != null && jsonObject.has("currentMigration")) {
                     var5 = jsonObject.get("currentMigration").getAsLong();
                     break label95;
                  }
               } catch (Throwable var11) {
                  try {
                     reader.close();
                  } catch (Throwable var7) {
                     var11.addSuppressed(var7);
                  }

                  throw var11;
               }

               reader.close();
               return 0L;
            }

            reader.close();
            return var5;
         } catch (IOException e) {
            this.plugin.getLogger().error("Error reading migrations.json file.", e);
         }
      }

      return 0L;
   }

   private void saveCurrentPatchToFile(long currentPatch) {
      File configFile = new File(this.plugin.getDataFolder(), "patchs.json");
      if (!configFile.exists()) {
         try {
            configFile.createNewFile();
         } catch (IOException e) {
            this.plugin.getLogger().error("Error creating patchs.json file.", e);
            return;
         }
      }

      try {
         FileWriter writer = new FileWriter(configFile);

         try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("currentPatch", currentPatch);
            GSON.toJson(jsonObject, writer);
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
         this.plugin.getLogger().error("Error writing patchs.json file.", e);
      }
   }

   public abstract static class Patch<T extends Plugin> {
      private final String currentDate;

      public abstract void run(T var1) throws Exception;

      @Generated
      public String getCurrentDate() {
         return this.currentDate;
      }

      @Generated
      public Patch(String currentDate) {
         this.currentDate = currentDate;
      }
   }

   @FunctionalInterface
   public interface PatchRunner {
      void run() throws Exception;
   }
}
