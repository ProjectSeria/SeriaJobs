package id.seria.gacha.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Files {
   public Files() {
   }

   public static void cloneDirectory(Path source, Path target) throws IOException {
      java.nio.file.Files.walk(source).forEach(sourcePath -> {
         Path targetPath = target.resolve(source.relativize(sourcePath));

         try {
            if (java.nio.file.Files.isDirectory(sourcePath)) {
               if (!java.nio.file.Files.exists(targetPath)) {
                  java.nio.file.Files.createDirectory(targetPath);
               }
            } else {
               java.nio.file.Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
         } catch (IOException e) {
            throw new RuntimeException("Error cloning directory: " + e.getMessage(), e);
         }
      });
   }

   public static void pruneEmptyDirectories(File root) {
      File[] children = root.listFiles();
      if (children != null) {
         for (File child : children) {
            if (child.isDirectory()) {
               pruneEmptyDirectories(child);
               File[] remaining = child.listFiles();
               if (remaining != null && remaining.length == 0) {
                  child.delete();
               }
            }
         }
      }
   }
}
