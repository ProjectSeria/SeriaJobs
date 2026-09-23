package id.seria.gacha.lib.common.services.services;

import id.seria.gacha.lib.common.services.factory.ServicesFactory;
import id.seria.gacha.lib.common.services.services.storage.PluginStorageObject;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StorageService implements ServicesFactory.ServiceLifecycle, Listener {
   private final ServerPlugin plugin;
   private final Map<JavaPlugin, PluginStorageObject> storages = new HashMap<>();

   public StorageService(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void load() {
   }

   @Override
   public void unload() {
      synchronized (this.storages) {
         this.storages.clear();
      }
   }

   public void registerPluginStorage(PluginStorageObject storage) {
      synchronized (this.storages) {
         this.storages.put(storage.getPlugin().getOriginal(), storage);
      }
   }

   public void unregisterPluginStorage(PluginStorageObject storage) {
      synchronized (this.storages) {
         this.storages.remove(storage.getPlugin());
      }
   }

   public PluginStorageObject removePluginStorage(ServerPlugin plugin) {
      synchronized (this.storages) {
         return this.storages.remove(plugin);
      }
   }

   @EventHandler
   protected void onPluginDisable(PluginDisableEvent event) {
      if (event.getPlugin() instanceof JavaPlugin) {
         JavaPlugin plugin = (JavaPlugin)event.getPlugin();
         synchronized (this.storages) {
            PluginStorageObject object = this.storages.remove(plugin);
            if (object != null) {
               object.unload();
               object.save(false);
            }
         }
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<JavaPlugin, PluginStorageObject> getStorages() {
      return this.storages;
   }
}
