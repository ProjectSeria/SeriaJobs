package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface ServerImplementation {
   @NotNull
   Plugin getOwningPlugin();

   boolean isOwnedByCurrentRegion(@NotNull Location var1);

   boolean isOwnedByCurrentRegion(@NotNull Location var1, int var2);

   boolean isOwnedByCurrentRegion(@NotNull Block var1);

   boolean isOwnedByCurrentRegion(@NotNull World var1, int var2, int var3);

   boolean isOwnedByCurrentRegion(@NotNull World var1, int var2, int var3, int var4);

   boolean isOwnedByCurrentRegion(@NotNull Entity var1);

   @NotNull
   GlobalSchedulerImplementation global();

   @NotNull
   AsyncSchedulerImplementation async();

   @NotNull
   EntitySchedulerImplementation entity(@NotNull Entity var1);

   @NotNull
   RegionSchedulerImplementation region(@NotNull World var1, int var2, int var3);

   @NotNull
   default RegionSchedulerImplementation region(@NotNull Location location) {
      World world = location.getWorld();
      if (world == null) {
         throw new IllegalArgumentException("Location world cannot be null");
      } else {
         return this.region(world, location.getBlockX() >> 4, location.getBlockZ() >> 4);
      }
   }

   @NotNull
   default RegionSchedulerImplementation region(@NotNull Block block) {
      return this.region(block.getWorld(), block.getX() >> 4, block.getZ() >> 4);
   }

   @NotNull
   default RegionSchedulerImplementation region(@NotNull Chunk chunk) {
      return this.region(chunk.getWorld(), chunk.getX(), chunk.getZ());
   }

   void cancelTasks();

   @NotNull
   default CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location location) {
      return this.teleportAsync(entity, location, TeleportCause.PLUGIN);
   }

   @NotNull
   CompletableFuture<Boolean> teleportAsync(@NotNull Entity var1, @NotNull Location var2, @NotNull TeleportCause var3);
}
