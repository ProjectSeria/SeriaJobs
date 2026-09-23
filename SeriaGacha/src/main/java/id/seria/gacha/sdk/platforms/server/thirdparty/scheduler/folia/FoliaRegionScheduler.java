package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.folia;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.RegionSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FoliaRegionScheduler implements RegionSchedulerImplementation {
   @NotNull
   private final Plugin plugin;
   @NotNull
   private final RegionScheduler regionScheduler;
   @NotNull
   private final World world;
   private final int chunkX;
   private final int chunkZ;

   public FoliaRegionScheduler(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ) {
      this.plugin = plugin;
      this.regionScheduler = plugin.getServer().getRegionScheduler();
      this.world = world;
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }

   @NotNull
   private <T> Consumer<ScheduledTask> buildFoliaConsumer(
      @NotNull FoliaTask<T> taskImplementation, @NotNull Function<TaskImplementation<T>, T> callbackFunction
   ) {
      return scheduledTask -> {
         taskImplementation.setScheduledTask(scheduledTask);
         taskImplementation.setCallback(callbackFunction.apply(taskImplementation));
         taskImplementation.asFuture().complete(taskImplementation);
      };
   }

   @Override
   public void execute(@NotNull Runnable run) {
      this.regionScheduler.execute(this.plugin, this.world, this.chunkX, this.chunkZ, run);
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> run(@NotNull Function<TaskImplementation<T>, T> function) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.regionScheduler.run(this.plugin, this.world, this.chunkX, this.chunkZ, foliaConsumer);
      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> function, long delay) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.regionScheduler.runDelayed(this.plugin, this.world, this.chunkX, this.chunkZ, foliaConsumer, delay);
      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> function, long delay, long period) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.regionScheduler.runAtFixedRate(this.plugin, this.world, this.chunkX, this.chunkZ, foliaConsumer, delay, period);
      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }
}
