package id.seria.gacha.sdk.platforms.server.components;

import id.seria.gacha.api.sdk.scheduler.Scheduler;
import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import id.seria.gacha.sdk.platforms.server.internal.ServerUtilities;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.ServerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.bukkit.BukkitServer;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.folia.FoliaServer;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class ServerScheduler implements Scheduler {
   private final ServerPlugin plugin;
   private final ServerImplementation implementation;
   private final boolean folia;
   private static final Method IS_OWNED_BY_REGION_LOCATION = findServerMethod("isOwnedByCurrentRegion", Location.class);
   private static final Method IS_OWNED_BY_REGION_ENTITY = findServerMethod("isOwnedByCurrentRegion", Entity.class);
   private static final Method TELEPORT_ASYNC = findTeleportAsync();
   private static final Method GET_CHUNK_AT_ASYNC = findGetChunkAtAsync();

   public ServerScheduler(ServerPlugin plugin) {
      this.plugin = plugin;
      this.folia = ServerUtilities.isFoliaServer();
      this.implementation = this.createSchedulerImplementation(plugin);
   }

   private ServerImplementation createSchedulerImplementation(ServerPlugin plugin) {
      try {
         if (plugin.isFoliaSupported() && ServerUtilities.isFoliaServer()) {
            try {
               Server.class.getMethod("isOwnedByCurrentRegion", Location.class);
               return FoliaServer.class.asSubclass(ServerImplementation.class).getConstructor(Plugin.class).newInstance(plugin.getOriginal());
            } catch (NoSuchMethodException var3) {
            }
         }

         return BukkitServer.class.asSubclass(ServerImplementation.class).getConstructor(Plugin.class).newInstance(plugin.getOriginal());
      } catch (Throwable ex) {
         throw new RuntimeException("Failed to initialize scheduler", ex);
      }
   }

   @Override
   public Task runSync(Runnable task) {
      return new ServerTask(this.implementation.global().run(task));
   }

   @Override
   public Task runSync(Runnable task, long delay) {
      return new ServerTask(this.implementation.global().runDelayed(task, Math.max(delay, 1L)));
   }

   @Override
   public Task scheduleSync(Runnable task, long delay, long period) {
      return new ServerTask(this.implementation.global().runAtFixedRate(task, Math.max(delay, 1L), Math.max(period, 1L)));
   }

   public Task runSync(Location loc, Runnable task) {
      return new ServerTask(this.implementation.region(loc).run(task));
   }

   public Task runSync(Location loc, Runnable task, long delay) {
      return new ServerTask(this.implementation.region(loc).runDelayed(task, Math.max(delay, 1L)));
   }

   public Task scheduleSync(Location loc, Runnable task, long delay, long period) {
      return new ServerTask(this.implementation.region(loc).runAtFixedRate(task, Math.max(delay, 1L), Math.max(period, 1L)));
   }

   public Task runSync(Chunk chunk, Runnable task) {
      return new ServerTask(this.implementation.region(chunk).run(task));
   }

   public Task runSync(Chunk chunk, Runnable task, long delay) {
      return new ServerTask(this.implementation.region(chunk).runDelayed(task, Math.max(delay, 1L)));
   }

   public Task scheduleSync(Chunk chunk, Runnable task, long delay, long period) {
      return new ServerTask(this.implementation.region(chunk).runAtFixedRate(task, Math.max(delay, 1L), Math.max(period, 1L)));
   }

   public Task runSync(Block block, Runnable task) {
      return new ServerTask(this.implementation.region(block).run(task));
   }

   public Task runSync(Block block, Runnable task, long delay) {
      return new ServerTask(this.implementation.region(block).runDelayed(task, Math.max(delay, 1L)));
   }

   public Task scheduleSync(Block block, Runnable task, long delay, long period) {
      return new ServerTask(this.implementation.region(block).runAtFixedRate(task, Math.max(delay, 1L), Math.max(period, 1L)));
   }

   public Task runSync(Entity entity, Runnable task) {
      return new ServerTask(this.implementation.entity(entity).run(task));
   }

   public Task runSync(Entity entity, Runnable task, long delay) {
      return new ServerTask(this.implementation.entity(entity).runDelayed(task, Math.max(delay, 1L)));
   }

   public Task scheduleSync(Entity entity, Runnable task, long delay, long period) {
      return new ServerTask(this.implementation.entity(entity).runAtFixedRate(task, Math.max(delay, 1L), Math.max(period, 1L)));
   }

   public Task runAsync(Entity entity, Runnable task) {
      return ServerUtilities.isFoliaServer() ? new ServerTask(this.implementation.entity(entity).run(task)) : this.runAsync(task);
   }

   public Task runAsync(Entity entity, Runnable task, long delay) {
      return ServerUtilities.isFoliaServer() ? new ServerTask(this.implementation.entity(entity).runDelayed(task, Math.max(delay, 1L))) : this.runAsync(task);
   }

   @Override
   public Task runAsync(Runnable task) {
      return new ServerTask(this.implementation.async().runNow(task));
   }

   @Override
   public Task runAsync(Runnable task, long delay) {
      return new ServerTask(this.implementation.async().runDelayed(task, Math.max(delay, 1L)));
   }

   @Override
   public Task scheduleAsync(Runnable task, long delay, long period) {
      return new ServerTask(this.implementation.async().runAtFixedRate(task, Math.max(delay, 1L), Math.max(period, 1L)));
   }

   @Override
   public <T> Future<T> callSyncMethod(Callable<T> task) {
      if (ServerUtilities.isFoliaServer()) {
         CompletableFuture<T> future = new CompletableFuture<>();
         this.implementation.global().run(() -> {
            try {
               future.complete(task.call());
            } catch (Exception e) {
               future.completeExceptionally(e);
            }
         });
         return future;
      } else {
         return Bukkit.getScheduler().callSyncMethod(this.plugin.getOriginal(), task);
      }
   }

   private static Method findServerMethod(String name, Class<?> parameter) {
      try {
         return Server.class.getMethod(name, parameter);
      } catch (NoSuchMethodException e) {
         return null;
      }
   }

   private static Method findTeleportAsync() {
      try {
         return Entity.class.getMethod("teleportAsync", Location.class);
      } catch (NoSuchMethodException e) {
         return null;
      }
   }

   private static Method findGetChunkAtAsync() {
      try {
         return World.class.getMethod("getChunkAtAsync", Location.class);
      } catch (NoSuchMethodException e) {
         return null;
      }
   }

   public boolean isFolia() {
      return this.folia;
   }

   public boolean isOwnedByCurrentThread(Location location) {
      if (this.isFolia() && IS_OWNED_BY_REGION_LOCATION != null && location != null) {
         try {
            return (Boolean)IS_OWNED_BY_REGION_LOCATION.invoke(Bukkit.getServer(), location);
         } catch (Throwable e) {
            return false;
         }
      } else {
         return Bukkit.isPrimaryThread();
      }
   }

   public boolean isOwnedByCurrentThread(Entity entity) {
      if (this.isFolia() && IS_OWNED_BY_REGION_ENTITY != null && entity != null) {
         try {
            return (Boolean)IS_OWNED_BY_REGION_ENTITY.invoke(Bukkit.getServer(), entity);
         } catch (Throwable e) {
            return false;
         }
      } else {
         return Bukkit.isPrimaryThread();
      }
   }

   public void execute(Entity entity, Runnable task) {
      if (this.isOwnedByCurrentThread(entity)) {
         task.run();
      } else {
         this.runSync(entity, task);
      }
   }

   public void execute(Location location, Runnable task) {
      if (this.isOwnedByCurrentThread(location)) {
         task.run();
      } else {
         this.runSync(location, task);
      }
   }

   public void execute(Runnable task) {
      if (!this.isFolia() && Bukkit.isPrimaryThread()) {
         task.run();
      } else {
         this.runSync(task);
      }
   }

   public CompletableFuture<Boolean> teleport(Entity entity, Location location) {
      CompletableFuture<Boolean> result = new CompletableFuture<>();
      this.prepareDestination(location).whenComplete((chunk, chunkError) -> this.execute(entity, () -> {
         try {
            for (Entity passenger : entity.getPassengers()) {
               passenger.leaveVehicle();
            }
         } catch (Throwable var7) {
         }

         if (this.isFolia() && TELEPORT_ASYNC != null) {
            try {
               CompletableFuture<Boolean> teleport = (CompletableFuture<Boolean>)TELEPORT_ASYNC.invoke(entity, location);
               teleport.whenComplete((success, error) -> {
                  if (error != null) {
                     result.completeExceptionally(error);
                  } else {
                     result.complete(success);
                  }
               });
            } catch (Throwable e) {
               result.completeExceptionally(e);
            }
         } else {
            result.complete(entity.teleport(location));
         }
      }));
      return result;
   }

   private CompletableFuture<Chunk> prepareDestination(Location location) {
      if (this.isFolia() && GET_CHUNK_AT_ASYNC != null && location != null && location.getWorld() != null) {
         try {
            CompletableFuture<Chunk> load = (CompletableFuture<Chunk>)GET_CHUNK_AT_ASYNC.invoke(location.getWorld(), location);
            return load.exceptionally(error -> null);
         } catch (Throwable e) {
            return CompletableFuture.completedFuture(null);
         }
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }
}
