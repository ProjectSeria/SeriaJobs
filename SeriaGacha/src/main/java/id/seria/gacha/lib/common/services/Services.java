package id.seria.gacha.lib.common.services;

import id.seria.gacha.lib.common.services.factory.ServicesFactory;
import id.seria.gacha.lib.common.services.services.AnimatorService;
import id.seria.gacha.lib.common.services.services.DatabaseService;
import id.seria.gacha.lib.common.services.services.HologramService;
import id.seria.gacha.lib.common.services.services.MessagesService;
import id.seria.gacha.lib.common.services.services.StorageService;
import id.seria.gacha.lib.common.services.services.WatcherService;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.Objects;

public class Services {
   private static ServerPlugin PLUGIN;
   private static ServicesFactory FACTORY;

   public Services() {
   }

   public static void enable(ServerPlugin plugin) throws Exception {
      if (PLUGIN == null) {
         FACTORY = new ServicesFactory(plugin);
         FACTORY.registerService(new DatabaseService(plugin));
         FACTORY.registerService(new MessagesService(plugin));
         FACTORY.registerService(new WatcherService(plugin));
         FACTORY.registerService(new HologramService(plugin));
         FACTORY.registerService(new StorageService(plugin));
         FACTORY.registerService(new AnimatorService(plugin));
         FACTORY.load();
         PLUGIN = plugin;
      }
   }

   public static void disable() {
      if (PLUGIN != null) {
         FACTORY.unload();
         PLUGIN = null;
      }
   }

   public static MessagesService getMessagesService() {
      Objects.requireNonNull(FACTORY, "Services did not initialize correctly.");
      return FACTORY.getServiceOrNull(MessagesService.class);
   }

   public static DatabaseService getDatabaseService() {
      Objects.requireNonNull(FACTORY, "Services did not initialize correctly.");
      return FACTORY.getServiceOrNull(DatabaseService.class);
   }

   public static WatcherService getWatcherService() {
      Objects.requireNonNull(FACTORY, "Services did not initialize correctly.");
      return FACTORY.getServiceOrNull(WatcherService.class);
   }

   public static HologramService getHologramService() {
      Objects.requireNonNull(FACTORY, "Services did not initialize correctly.");
      return FACTORY.getServiceOrNull(HologramService.class);
   }

   public static StorageService getStorageService() {
      Objects.requireNonNull(FACTORY, "Services did not initialize correctly.");
      return FACTORY.getServiceOrNull(StorageService.class);
   }

   public static AnimatorService getAnimatorService() {
      Objects.requireNonNull(FACTORY, "Services did not initialize correctly.");
      return FACTORY.getServiceOrNull(AnimatorService.class);
   }
}
