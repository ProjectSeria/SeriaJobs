package id.seria.gacha.limbo.debug;

import id.seria.gacha.Crates;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DebugExecutorMonitor {
   private final ServerPlugin plugin;
   private final Logger debugLogger;
   private FileHandler fileHandler;

   public DebugExecutorMonitor(ServerPlugin plugin) {
      this.plugin = plugin;
      this.debugLogger = Logger.getLogger(plugin.getName() + "-Debugger");
      this.debugLogger.setUseParentHandlers(false);

      try {
         File debugLogFile = new File(plugin.getDataFolder(), "debug.log");
         if (!debugLogFile.exists()) {
            debugLogFile.getParentFile().mkdirs();
            debugLogFile.createNewFile();
         }

         this.fileHandler = new FileHandler(debugLogFile.getAbsolutePath(), true);
         this.fileHandler.setFormatter(new SimpleFormatter());
         this.debugLogger.addHandler(this.fileHandler);
      } catch (IOException e) {
         plugin.getLogger().warn("Could not create debug.log file handler", e);
      }
   }

   public void debug(String message) {
      if (Crates.isDebugEnabled()) {
         this.debugLogger.info(message);
      }
   }

   public void logExecutorStats() {
      try {
         ThreadPoolExecutor exec = (ThreadPoolExecutor)Utilities.getAsyncService();
         String stats = String.format(
            "Executor stats - Active: %d, PoolSize: %d, LargestPoolSize: %d, CompletedTasks: %d, TaskCount: %d, QueueSize: %d",
            exec.getActiveCount(),
            exec.getPoolSize(),
            exec.getLargestPoolSize(),
            exec.getCompletedTaskCount(),
            exec.getTaskCount(),
            exec.getQueue().size()
         );
         this.debug(stats);
      } catch (Exception e) {
         this.debug("Failed to log executor stats: " + e.getMessage());
      }
   }

   public void startPeriodicLogging() {
      this.plugin.getScheduler().scheduleAsync(this::logExecutorStats, 0L, 100L);
   }

   public void close() {
      if (this.fileHandler != null) {
         this.fileHandler.close();
      }
   }
}
