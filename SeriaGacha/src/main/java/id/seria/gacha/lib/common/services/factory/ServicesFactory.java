package id.seria.gacha.lib.common.services.factory;

import com.google.common.collect.Lists;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Generated;

public class ServicesFactory {
   private final ServerPlugin plugin;
   private final List<ServicesFactory.ServiceLifecycle> services = new ArrayList<>();

   public ServicesFactory(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   public void load() throws Exception {
      synchronized (this.services) {
         for (ServicesFactory.ServiceLifecycle service : this.services) {
            service.load();
         }
      }
   }

   public void unload() {
      synchronized (this.services) {
         List<ServicesFactory.ServiceLifecycle> copy = Lists.newArrayList(this.services);
         Collections.reverse(copy);

         for (ServicesFactory.ServiceLifecycle service : copy) {
            try {
               service.unload();
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }
   }

   public synchronized void registerService(ServicesFactory.ServiceLifecycle service) {
      synchronized (this.services) {
         this.services.add(service);
      }
   }

   public <T> T getServiceOrNull(Class<T> clazz) {
      synchronized (this.services) {
         for (ServicesFactory.ServiceLifecycle service : this.services) {
            Object var10000;
            try {
               var10000 = clazz.cast(service);
            } catch (ClassCastException var7) {
               continue;
            }

            return (T)var10000;
         }

         return null;
      }
   }

   public List<ServicesFactory.ServiceLifecycle> getServices() {
      synchronized (this.services) {
         return Collections.unmodifiableList(this.services);
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   public interface ServiceLifecycle {
      void load() throws Exception;

      void unload() throws Exception;
   }
}
