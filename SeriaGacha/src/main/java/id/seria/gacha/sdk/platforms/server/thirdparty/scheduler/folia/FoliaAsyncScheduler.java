package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.folia;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.AsyncSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class FoliaAsyncScheduler implements AsyncSchedulerImplementation {
   @NotNull
   private final Plugin plugin;
   @NotNull
   private final AsyncScheduler asyncScheduler;

   @Internal
   public FoliaAsyncScheduler(@NotNull Plugin plugin) {
      this.plugin = plugin;
      this.asyncScheduler = plugin.getServer().getAsyncScheduler();
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

   @NotNull
   @Override
   public <T> TaskImplementation<T> runNow(@NotNull Function<TaskImplementation<T>, T> function) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.asyncScheduler.runNow(this.plugin, foliaConsumer);
      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> function, long delay, @NotNull TimeUnit unit) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.asyncScheduler.runDelayed(this.plugin, foliaConsumer, delay, unit);
      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> function, long delay, long period, @NotNull TimeUnit unit) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.asyncScheduler.runAtFixedRate(this.plugin, foliaConsumer, delay, period, unit);
      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @Override
   public void cancelTasks() {
      this.asyncScheduler.cancelTasks(this.plugin);
   }
}
