package id.seria.gacha.lib.common.utils.configuration.workers;

import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.objects.LazyObject;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class AsyncConfigurationWorker extends ConfigurationWorker {
   protected final LazyObject<ExecutorService> lazyExecutor = new LazyObject<ExecutorService>() {
      public ExecutorService initialize() {
         return Executors.newSingleThreadExecutor();
      }
   };
   protected Listener listener;

   public AsyncConfigurationWorker(ServerPlugin plugin, Configuration configuration, String sourceFileName, String targetFileName) {
      super(plugin, configuration, sourceFileName, targetFileName);
   }

   public AsyncConfigurationWorker(ServerPlugin plugin, Configuration configuration, String fileName) {
      super(plugin, configuration, fileName);
   }

   @Override
   public void save() {
      if (this.plugin.isEnabled()) {
         try {
            this.lazyExecutor.get().execute(() -> super.save());
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else {
         this.saveNow();
      }
   }

   public void saveNow() {
      super.save();
   }

   public void open() {
      this.disposeListener();
      this.listener = new Listener() {
         @EventHandler
         public void onPluginDisable(PluginDisableEvent event) {
            if (event.getPlugin().equals(AsyncConfigurationWorker.this.plugin)) {
               AsyncConfigurationWorker.this.close();
            }
         }
      };
      this.plugin.getEventBus().registerListener(this.listener);
   }

   public void close() {
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

   private void disposeListener() {
      if (this.listener != null) {
         HandlerList.unregisterAll(this.listener);
         this.listener = null;
      }
   }
}
