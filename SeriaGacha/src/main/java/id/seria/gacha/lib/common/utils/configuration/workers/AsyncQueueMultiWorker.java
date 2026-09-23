package id.seria.gacha.lib.common.utils.configuration.workers;

import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.lib.common.utils.objects.LazyObject;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class AsyncQueueMultiWorker {
   protected ServerPlugin plugin;
   protected final LazyObject<ExecutorService> lazyExecutor = new LazyObject<ExecutorService>() {
      public ExecutorService initialize() {
         return Executors.newCachedThreadPool();
      }
   };
   protected Listener listener;
   private AtomicBoolean mutex = new AtomicBoolean();
   private Task task;
   private int updateInterval = 60;
   protected List<ConfigurationWorker> queuedConfigurations = new ArrayList<>();

   public AsyncQueueMultiWorker(ServerPlugin plugin, int updateInterval) {
      this.plugin = plugin;
      this.updateInterval = updateInterval;
   }

   public AsyncQueueMultiWorker(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   public void saveLater(ConfigurationWorker worker) {
      if (this.plugin.isEnabled()) {
         try {
            this.lazyExecutor.get().execute(worker::save);
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else {
         this.saveNow(worker);
      }
   }

   public void saveNow(ConfigurationWorker worker) {
      worker.save();
   }

   public void queueSave(ConfigurationWorker worker) {
      if (!this.queuedConfigurations.contains(worker)) {
         this.queuedConfigurations.add(worker);
      }
   }

   public void open() {
      this.disposeListener();
      this.listener = new Listener() {
         @EventHandler
         public void onPluginDisable(PluginDisableEvent event) {
            if (event.getPlugin().equals(AsyncQueueMultiWorker.this.plugin)) {
               AsyncQueueMultiWorker.this.close();
            }
         }
      };
      this.plugin.getEventBus().registerListener(this.listener);
      this.disposeTask();
      this.task = this.plugin.getScheduler().scheduleSync(() -> {
         synchronized (this.queuedConfigurations) {
            if (!this.mutex.get() && this.queuedConfigurations.size() > 0) {
               this.mutex.set(true);

               try {
                  this.lazyExecutor.get().execute(() -> {
                     this.queuedConfigurations.forEach(ConfigurationWorker::save);
                     this.queuedConfigurations.clear();
                     this.mutex.set(false);
                  });
               } catch (Exception e) {
                  this.mutex.set(false);
                  e.printStackTrace();
               }
            }
         }
      }, this.updateInterval, this.updateInterval);
   }

   public void close() {
      if (!this.mutex.get() && this.queuedConfigurations.size() > 0) {
         this.mutex.set(true);

         try {
            this.lazyExecutor.get().execute(() -> {
               try {
                  this.queuedConfigurations.forEach(ConfigurationWorker::save);
                  this.queuedConfigurations.clear();
               } catch (Exception e) {
                  e.printStackTrace();
               } finally {
                  this.mutex.set(false);
               }
            });
         } catch (Exception e) {
            this.mutex.set(false);
            e.printStackTrace();
         }
      }

      this.disposeTask();

      try {
         if (this.lazyExecutor.isInitialized()) {
            this.lazyExecutor.get().shutdown();

            try {
               this.lazyExecutor.get().awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }

            this.lazyExecutor.dispose();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      this.disposeListener();
   }

   private void disposeTask() {
      if (this.task != null) {
         this.task.cancel();
         this.task = null;
      }
   }

   private void disposeListener() {
      if (this.listener != null) {
         HandlerList.unregisterAll(this.listener);
         this.listener = null;
      }
   }
}
