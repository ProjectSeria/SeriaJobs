package id.seria.gacha.lib.common.utils.configuration.workers;

import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncQueueConfigurationWorker extends AsyncConfigurationWorker {
   private AtomicBoolean queued = new AtomicBoolean();
   private AtomicBoolean mutex = new AtomicBoolean();
   private Task task;
   private int updateInterval = 60;

   public AsyncQueueConfigurationWorker(ServerPlugin plugin, Configuration configuration, String sourceFileName, String targetFileName, int updateInterval) {
      super(plugin, configuration, sourceFileName, targetFileName);
      this.updateInterval = updateInterval;
   }

   public AsyncQueueConfigurationWorker(ServerPlugin plugin, Configuration configuration, String fileName, int updateInterval) {
      super(plugin, configuration, fileName);
      this.updateInterval = updateInterval;
   }

   public AsyncQueueConfigurationWorker(ServerPlugin plugin, Configuration configuration, String sourceFileName, String targetFileName) {
      super(plugin, configuration, sourceFileName, targetFileName);
   }

   public AsyncQueueConfigurationWorker(ServerPlugin plugin, Configuration configuration, String fileName) {
      super(plugin, configuration, fileName);
   }

   public void queueSave() {
      this.queued.set(true);
   }

   @Override
   public void open() {
      super.open();
      this.disposeTask();
      this.task = this.plugin.getScheduler().scheduleSync(() -> {
         if (!this.mutex.get() && this.queued.get()) {
            this.mutex.set(true);

            try {
               this.lazyExecutor.get().execute(() -> {
                  super.saveNow();
                  this.mutex.set(false);
               });
            } catch (Exception e) {
               this.mutex.set(false);
               e.printStackTrace();
            }

            this.queued.set(false);
         }
      }, this.updateInterval, this.updateInterval);
   }

   @Override
   public void close() {
      if (this.queued.get()) {
         try {
            this.lazyExecutor.get().execute(() -> super.saveNow());
            this.queued.set(false);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      this.disposeTask();
      super.close();
   }

   private void disposeTask() {
      if (this.task != null) {
         this.task.cancel();
         this.task = null;
      }
   }
}
