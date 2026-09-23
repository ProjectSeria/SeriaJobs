package id.seria.gacha.extensions.logger.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogHandler {
   private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
   private File logFile;
   private FileWriter logWriter;
   private BufferedWriter bufferedWriter;
   private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "PhoenixCrates-Logger");
      t.setDaemon(true);
      return t;
   });

   public LogHandler() {
   }

   public void init(File logFolder) {
      if (!logFolder.exists()) {
         logFolder.mkdirs();
      }

      String fileName = "latest.log";
      this.logFile = new File(logFolder, "latest.log");
      if (this.logFile.exists()) {
         this.archiveLogs();
      }

      try {
         this.logWriter = new FileWriter(this.logFile);
         this.bufferedWriter = new BufferedWriter(this.logWriter);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void log(Level level, String message) {
      if (this.bufferedWriter != null) {
         String timestamp = dateFormat.format(new Date());
         String logMessage = "[" + timestamp + "][" + level + "] " + message;
         this.executor.execute(() -> {
            try {
               if (this.bufferedWriter == null) {
                  return;
               }

               this.bufferedWriter.write(logMessage);
               this.bufferedWriter.newLine();
               this.bufferedWriter.flush();
            } catch (IOException e) {
               e.printStackTrace();
            }
         });
      }
   }

   public void close() {
      this.executor.shutdown();

      try {
         if (!this.executor.awaitTermination(5L, TimeUnit.SECONDS)) {
            this.executor.shutdownNow();
         }
      } catch (InterruptedException ignored) {
         this.executor.shutdownNow();
      }

      try {
         if (this.bufferedWriter != null) {
            this.bufferedWriter.flush();
            this.bufferedWriter.close();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void archiveLogs() {
      try {
         String archivedFileName = dateFormat.format(new Date()) + ".log";
         File archivedFile = new File(this.logFile.getParentFile(), archivedFileName);
         FileReader fr = new FileReader(this.logFile);

         label94: {
            try {
               BufferedReader br = new BufferedReader(fr);

               label90: {
                  try {
                     if (this.logFile.length() == 0L) {
                        break label90;
                     }

                     FileOutputStream fos = new FileOutputStream(archivedFile + ".gz");

                     try {
                        ZipOutputStream zos = new ZipOutputStream(fos);

                        try {
                           zos.putNextEntry(new ZipEntry(archivedFileName));

                           int c;
                           while ((c = br.read()) != -1) {
                              zos.write(c);
                           }

                           zos.closeEntry();
                        } catch (Throwable var13) {
                           try {
                              zos.close();
                           } catch (Throwable var12) {
                              var13.addSuppressed(var12);
                           }

                           throw var13;
                        }

                        zos.close();
                     } catch (Throwable var14) {
                        try {
                           fos.close();
                        } catch (Throwable var11) {
                           var14.addSuppressed(var11);
                        }

                        throw var14;
                     }

                     fos.close();
                  } catch (Throwable var15) {
                     try {
                        br.close();
                     } catch (Throwable var10) {
                        var15.addSuppressed(var10);
                     }

                     throw var15;
                  }

                  br.close();
                  break label94;
               }

               br.close();
            } catch (Throwable var16) {
               try {
                  fr.close();
               } catch (Throwable var9) {
                  var16.addSuppressed(var9);
               }

               throw var16;
            }

            fr.close();
            return;
         }

         fr.close();
         this.logFile.delete();
         this.logFile.createNewFile();
         this.logWriter = new FileWriter(this.logFile, true);
         this.bufferedWriter = new BufferedWriter(this.logWriter);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
