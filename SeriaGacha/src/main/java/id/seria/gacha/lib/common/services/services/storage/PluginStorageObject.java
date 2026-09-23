package id.seria.gacha.lib.common.services.services.storage;

import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.workers.AsyncConfigurationWorker;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import lombok.Generated;

public abstract class PluginStorageObject extends Configuration {
   protected final ServerPlugin plugin;
   protected final AsyncConfigurationWorker worker;

   public PluginStorageObject(ServerPlugin plugin) {
      this(plugin, "cache.yml");
   }

   public PluginStorageObject(ServerPlugin plugin, String fileName) {
      this.plugin = plugin;
      this.worker = new AsyncConfigurationWorker(plugin, this, fileName, fileName);
   }

   public PluginStorageObject(ServerPlugin plugin, String sourceFileName, String targetFileName) {
      this.plugin = plugin;
      this.worker = new AsyncConfigurationWorker(plugin, this, sourceFileName, targetFileName);
   }

   public void load() throws Exception {
      this.worker.setSilent(true);
      this.worker.open();
      this.worker.validate();
   }

   public void unload() {
      this.worker.close();
   }

   public void save(boolean async) {
      if (async) {
         this.worker.save();
      } else {
         this.worker.saveNow();
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public AsyncConfigurationWorker getWorker() {
      return this.worker;
   }
}
