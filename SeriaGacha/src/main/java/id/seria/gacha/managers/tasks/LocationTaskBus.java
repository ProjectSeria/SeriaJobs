package id.seria.gacha.managers.tasks;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.scheduler.Task;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;

public class LocationTaskBus {
   private static final Runnable NO_OP_RELEASE = () -> {};
   private final ConcurrentHashMap<LocationTaskBus.LocationKey, LocationTaskBus.Channel> channels = new ConcurrentHashMap<>();

   public LocationTaskBus() {
   }

   public void subscribe(TaskSubscriber subscriber) {
      this.subscribe(subscriber, null);
   }

   public void subscribe(TaskSubscriber subscriber, Runnable onRelease) {
      if (subscriber != null && subscriber.getLocation() != null && subscriber.getLocation().getWorld() != null) {
         Location location = subscriber.getLocation();
         LocationTaskBus.LocationKey key = LocationTaskBus.LocationKey.from(location);
         Runnable releaseCallback = onRelease == null ? NO_OP_RELEASE : onRelease;
         this.channels.compute(key, (ignored, channel) -> {
            if (channel == null) {
               channel = new LocationTaskBus.Channel(key, location);
            }

            channel.subscribers.put(subscriber, releaseCallback);
            return (LocationTaskBus.Channel)channel;
         });
      }
   }

   public void unsubscribe(TaskSubscriber subscriber) {
      if (subscriber != null && subscriber.getLocation() != null && subscriber.getLocation().getWorld() != null) {
         LocationTaskBus.LocationKey key = LocationTaskBus.LocationKey.from(subscriber.getLocation());
         LocationTaskBus.Channel channel = this.channels.get(key);
         if (channel != null) {
            channel.finishSubscriber(subscriber);
            if (channel.subscribers.isEmpty()) {
               channel.cancel();
               this.channels.remove(key, channel);
            }
         }
      }
   }

   public void shutdown() {
      for (LocationTaskBus.Channel channel : this.channels.values()) {
         channel.cancel();
      }

      this.channels.clear();
   }

   private final class Channel {
      private final LocationTaskBus.LocationKey key;
      private final ConcurrentHashMap<TaskSubscriber, Runnable> subscribers;
      private final Task task;

      private Channel(final LocationTaskBus.LocationKey key, final Location location) {
         this.key = key;
         this.subscribers = new ConcurrentHashMap<>();
         this.task = Crates.getScheduler().scheduleSync(location, this::tick, 1L, 1L);
      }

      private void tick() {
         for (Entry<TaskSubscriber, Runnable> entry : new ArrayList<>(this.subscribers.entrySet())) {
            TaskSubscriber subscriber = entry.getKey();
            if (subscriber.isFinished()) {
               this.finishSubscriber(subscriber);
            } else {
               try {
                  subscriber.tick();
               } catch (Exception ex) {
                  Crates.getLogger().error("Error while ticking location subscriber", ex);
               }

               if (subscriber.isFinished()) {
                  this.finishSubscriber(subscriber);
               }
            }
         }

         if (this.subscribers.isEmpty()) {
            this.cancel();
            LocationTaskBus.this.channels.remove(this.key, this);
         }
      }

      private void finishSubscriber(TaskSubscriber subscriber) {
         Runnable onRelease = this.subscribers.remove(subscriber);
         if (onRelease != null && onRelease != LocationTaskBus.NO_OP_RELEASE) {
            try {
               onRelease.run();
            } catch (Exception ex) {
               Crates.getLogger().error("Error while releasing location subscriber", ex);
            }
         }
      }

      private void cancel() {
         this.task.cancel();
      }
   }

   private static final class LocationKey {
      private final String world;
      private final int x;
      private final int y;
      private final int z;

      private LocationKey(String world, int x, int y, int z) {
         this.world = world;
         this.x = x;
         this.y = y;
         this.z = z;
      }

      private static LocationTaskBus.LocationKey from(Location location) {
         return new LocationTaskBus.LocationKey(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
      }

      @Override
      public boolean equals(Object other) {
         if (this == other) {
            return true;
         } else {
            return !(other instanceof LocationTaskBus.LocationKey key)
               ? false
               : this.x == key.x && this.y == key.y && this.z == key.z && Objects.equals(this.world, key.world);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.world, this.x, this.y, this.z);
      }
   }
}
