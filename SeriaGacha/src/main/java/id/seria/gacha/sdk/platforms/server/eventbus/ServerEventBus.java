package id.seria.gacha.sdk.platforms.server.eventbus;

import id.seria.gacha.api.sdk.plugin.eventbus.EventBus;
import id.seria.gacha.api.sdk.utilities.Validate;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class ServerEventBus implements EventBus {
   private final ServerPlugin plugin;
   private final ServerEventBus.EventBusListener listener = new ServerEventBus.EventBusListener();

   public ServerEventBus(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   public void initialize() {
   }

   public void shutdown() {
      HandlerList.unregisterAll(this.listener);
   }

   public void registerListener(Listener listener) {
      Bukkit.getPluginManager().registerEvents(listener, this.plugin.getOriginal());
   }

   public void unregisterListener(Listener listener) {
      HandlerList.unregisterAll(listener);
   }

   @Override
   public <T> void subscribe(Class<T> eventClass, Consumer<T> handler) {
      Validate.isTrue(Event.class.isAssignableFrom(eventClass), "Event is not bukkit");
      Bukkit.getPluginManager()
         .registerEvent((Class<? extends Event>)eventClass, this.listener, EventPriority.NORMAL, (listener, event) -> handler.accept((T)event), this.plugin.getOriginal());
   }

   @Override
   public void trigger(Object event) {
      Validate.isTrue(event instanceof Event, "Event is not bukkit");
      if (Bukkit.isPrimaryThread()) {
         Bukkit.getPluginManager().callEvent((Event)event);
      } else {
         this.plugin.getScheduler().runSync(() -> Bukkit.getPluginManager().callEvent((Event)event));
      }
   }

   private static class EventBusListener implements Listener {
      private EventBusListener() {
      }
   }
}
