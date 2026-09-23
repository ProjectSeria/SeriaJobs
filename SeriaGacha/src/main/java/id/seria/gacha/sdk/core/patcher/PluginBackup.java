package id.seria.gacha.sdk.core.patcher;

import id.seria.gacha.api.sdk.plugin.Plugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PluginBackup {
   public PluginBackup() {
   }

   public static void createBackup(Plugin plugin) {
      File pluginFolder = plugin.getDataFolder();
      String currentDate = getCurrentDate();
      File backupFolder = new File(pluginFolder, "backups");
      if (!backupFolder.exists()) {
         backupFolder.mkdirs();
      }

      File backupFile = new File(backupFolder, "backup_" + currentDate + ".zip");

      try {
         FileOutputStream fos = new FileOutputStream(backupFile);

         try {
            ZipOutputStream zos = new ZipOutputStream(fos);

            try {
               zipFolder(pluginFolder, pluginFolder.getName(), zos);
            } catch (Throwable var11) {
               try {
                  zos.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }

               throw var11;
            }

            zos.close();
         } catch (Throwable var12) {
            try {
               fos.close();
            } catch (Throwable var9) {
               var12.addSuppressed(var9);
            }

            throw var12;
         }

         fos.close();
      } catch (IOException e) {
         plugin.getLogger().error("Error while creating a backup", e);
      }
   }

   private static void zipFolder(File sourceFolder, String basePath, ZipOutputStream zos) throws IOException {
      File[] files = sourceFolder.listFiles();
      if (files != null) {
         for (File file : files) {
            if (!isIgnoredFolder(file)) {
               String entryName = basePath + File.separator + file.getName();
               if (file.isDirectory()) {
                  zipFolder(file, entryName, zos);
               } else {
                  zos.putNextEntry(new ZipEntry(entryName.replace("\\", "/")));
                  FileInputStream fis = new FileInputStream(file);

                  try {
                     byte[] buffer = new byte[1024];

                     int length;
                     while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                     }
                  } catch (Throwable var13) {
                     try {
                        fis.close();
                     } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                     }

                     throw var13;
                  }

                  fis.close();
                  zos.closeEntry();
               }
            }
         }
      }
   }

   private static boolean isIgnoredFolder(File file) {
      if (!file.isDirectory()) {
         return false;
      }

      String name = file.getName().toLowerCase();
      return name.equals("backups") || name.equals("debug_output");
   }

   private static String getCurrentDate() {
      LocalDateTime now = LocalDateTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
      return now.format(formatter);
   }
}
