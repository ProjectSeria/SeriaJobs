package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.bukkit;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.AsyncSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.EntitySchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.GlobalSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.RegionSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.ServerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.util.MethodInvoker;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.util.ReflectionUtil;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.util.WrappedReflectiveOperationException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class BukkitServer implements ServerImplementation {
   @NotNull
   private final Plugin owningPlugin;
   @NotNull
   private final BukkitSyncScheduler sync;
   @NotNull
   private final BukkitRegionScheduler region;
   @NotNull
   private final BukkitAsyncScheduler async;
   @Nullable
   private MethodInvoker teleportAsyncMethod;

   @Internal
   public BukkitServer(@NotNull Plugin owningPlugin) {
      this.owningPlugin = owningPlugin;
      this.sync = new BukkitSyncScheduler(owningPlugin);
      this.region = new BukkitRegionScheduler(owningPlugin);
      this.async = new BukkitAsyncScheduler(owningPlugin);

      try {
         this.teleportAsyncMethod = ReflectionUtil.getMethod(Entity.class, "teleportAsync", Location.class, TeleportCause.class);
      } catch (WrappedReflectiveOperationException var3) {
      }
   }

   @NotNull
   @Override
   public Plugin getOwningPlugin() {
      return this.owningPlugin;
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull Location location) {
      return this.owningPlugin.getServer().isPrimaryThread();
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull Location location, int squareRadiusChunks) {
      return this.owningPlugin.getServer().isPrimaryThread();
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull Block block) {
      return this.owningPlugin.getServer().isPrimaryThread();
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ) {
      return this.owningPlugin.getServer().isPrimaryThread();
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ, int squareRadiusChunks) {
      return this.owningPlugin.getServer().isPrimaryThread();
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull Entity entity) {
      return this.owningPlugin.getServer().isPrimaryThread();
   }

   @NotNull
   @Override
   public GlobalSchedulerImplementation global() {
      return this.sync;
   }

   @NotNull
   @Override
   public AsyncSchedulerImplementation async() {
      return this.async;
   }

   @NotNull
   @Override
   public EntitySchedulerImplementation entity(@NotNull Entity entity) {
      return new BukkitEntityScheduler(this.owningPlugin, entity);
   }

   @NotNull
   @Override
   public RegionSchedulerImplementation region(@NotNull World world, int chunkX, int chunkZ) {
      return this.region;
   }

   @Override
   public void cancelTasks() {
      this.owningPlugin.getServer().getScheduler().cancelTasks(this.owningPlugin);
   }

   @NotNull
   @Override
   public CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location location, @NotNull TeleportCause cause) {
      if (this.teleportAsyncMethod != null) {
         Object result = this.teleportAsyncMethod.invoke(entity, location, cause);
         return Objects.requireNonNull((CompletableFuture<Boolean>)result);
      } else {
         CompletableFuture<Boolean> future = new CompletableFuture<>();
         this.entity(entity).run(task -> {
            try {
               entity.teleport(location);
               future.complete(true);
            } catch (Throwable ex) {
               this.owningPlugin.getLogger().log(Level.SEVERE, "Failed to teleport entity", ex);
               future.complete(false);
            }
         });
         return future;
      }
   }
}
