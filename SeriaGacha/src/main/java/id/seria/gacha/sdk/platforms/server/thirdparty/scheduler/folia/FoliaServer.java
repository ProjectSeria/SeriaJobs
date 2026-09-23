package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.folia;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.AsyncSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.EntitySchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.GlobalSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.RegionSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.ServerImplementation;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class FoliaServer implements ServerImplementation {
   @NotNull
   private final Plugin owningPlugin;
   @NotNull
   private final FoliaGlobalScheduler globalScheduler;
   @NotNull
   private final FoliaAsyncScheduler asyncScheduler;

   @Internal
   public FoliaServer(@NotNull Plugin owningPlugin) {
      this.owningPlugin = owningPlugin;
      this.globalScheduler = new FoliaGlobalScheduler(owningPlugin);
      this.asyncScheduler = new FoliaAsyncScheduler(owningPlugin);
   }

   @NotNull
   @Override
   public Plugin getOwningPlugin() {
      return this.owningPlugin;
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull Location location) {
      return Bukkit.isOwnedByCurrentRegion(location);
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull Location location, int squareRadiusChunks) {
      return Bukkit.isOwnedByCurrentRegion(location, squareRadiusChunks);
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull Block block) {
      return Bukkit.isOwnedByCurrentRegion(block);
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ) {
      return Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ);
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ, int squareRadiusChunks) {
      return Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ, squareRadiusChunks);
   }

   @Override
   public boolean isOwnedByCurrentRegion(@NotNull Entity entity) {
      return Bukkit.isOwnedByCurrentRegion(entity);
   }

   @NotNull
   @Override
   public GlobalSchedulerImplementation global() {
      return this.globalScheduler;
   }

   @NotNull
   @Override
   public AsyncSchedulerImplementation async() {
      return this.asyncScheduler;
   }

   @NotNull
   @Override
   public EntitySchedulerImplementation entity(@NotNull Entity entity) {
      return new FoliaEntityScheduler(this.owningPlugin, entity);
   }

   @NotNull
   @Override
   public RegionSchedulerImplementation region(@NotNull World world, int chunkX, int chunkZ) {
      return new FoliaRegionScheduler(this.owningPlugin, world, chunkX, chunkZ);
   }

   @Override
   public void cancelTasks() {
      this.globalScheduler.cancelTasks();
      this.asyncScheduler.cancelTasks();
   }

   @NotNull
   @Override
   public CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location location, @NotNull TeleportCause cause) {
      return entity.teleportAsync(location, cause);
   }
}
