package id.seria.gacha.sdk.core;

import id.seria.gacha.api.sdk.plugin.Plugin;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;

public class Core {
   private static Plugin PLUGIN;
   public static ExecutorService ASYNC_SERVICE;
   private static ScheduledExecutorService SCHEDULER_SERVICE;

   public Core() {
   }

   public static void initialize(@NonNull Plugin plugin) throws Exception {
      if (plugin == null) {
         throw new NullPointerException("plugin is marked non-null but is null");
      }

      if (PLUGIN == null) {
         PLUGIN = plugin;
         ASYNC_SERVICE = Executors.newFixedThreadPool(Math.max(3, plugin.getConfiguration().getInternalPoolSize()), runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(plugin.getName() + "-Async-" + thread.getId());
            thread.setUncaughtExceptionHandler((t, e) -> plugin.getLogger().error("Uncaught exception in thread " + t.getName(), e));
            return thread;
         });
         SCHEDULER_SERVICE = Executors.newScheduledThreadPool(1);
         ServerIdentity.initialize(plugin);
      }
   }

   public static void shutdown() {
      if (PLUGIN != null) {
         if (ASYNC_SERVICE != null) {
            ASYNC_SERVICE.shutdown();

            try {
               ASYNC_SERVICE.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
               PLUGIN.getLogger().error("Interrupted while shutting down", e);
            }
         }

         if (SCHEDULER_SERVICE != null) {
            SCHEDULER_SERVICE.shutdownNow();
         }

         PLUGIN = null;
      }
   }

   public static ExecutorService getAsyncService() {
      Objects.requireNonNull(ASYNC_SERVICE, "Core not initialized correctly.");
      return ASYNC_SERVICE;
   }

   public static ScheduledExecutorService getSchedulerService() {
      Objects.requireNonNull(SCHEDULER_SERVICE, "Core not initialized correctly.");
      return SCHEDULER_SERVICE;
   }
}
