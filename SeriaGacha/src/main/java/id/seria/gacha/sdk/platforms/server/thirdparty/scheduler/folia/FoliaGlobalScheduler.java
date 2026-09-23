package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.folia;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.GlobalSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class FoliaGlobalScheduler implements GlobalSchedulerImplementation {
   @NotNull
   private final Plugin plugin;
   @NotNull
   private final GlobalRegionScheduler globalRegionScheduler;

   @Internal
   public FoliaGlobalScheduler(@NotNull Plugin plugin) {
      this.plugin = plugin;
      this.globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
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
      this.globalRegionScheduler.execute(this.plugin, run);
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> run(@NotNull Function<TaskImplementation<T>, T> function) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.globalRegionScheduler.run(this.plugin, foliaConsumer);
      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> function, long delay) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.globalRegionScheduler.runDelayed(this.plugin, foliaConsumer, delay);
      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> function, long delay, long period) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.globalRegionScheduler.runAtFixedRate(this.plugin, foliaConsumer, delay, period);
      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @Override
   public void cancelTasks() {
      this.globalRegionScheduler.cancelTasks(this.plugin);
   }
}
