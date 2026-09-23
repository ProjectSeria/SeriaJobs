package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.folia;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.EntitySchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class FoliaEntityScheduler implements EntitySchedulerImplementation {
   @NotNull
   private final Plugin plugin;
   @NotNull
   private final EntityScheduler entityScheduler;

   @Internal
   public FoliaEntityScheduler(@NotNull Plugin plugin, @NotNull Entity entity) {
      this.plugin = plugin;
      this.entityScheduler = entity.getScheduler();
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
   public boolean execute(@NotNull Runnable run, @Nullable Runnable retired, long delay) {
      return this.entityScheduler.execute(this.plugin, run, retired, delay);
   }

   @Nullable
   @Override
   public <T> TaskImplementation<T> run(@NotNull Function<TaskImplementation<T>, T> function, @Nullable Runnable retired) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.entityScheduler.run(this.plugin, foliaConsumer, retired);
      if (scheduledTask == null) {
         return null;
      }

      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @Nullable
   @Override
   public <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> function, @Nullable Runnable retired, long delay) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.entityScheduler.runDelayed(this.plugin, foliaConsumer, retired, delay);
      if (scheduledTask == null) {
         return null;
      }

      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }

   @Nullable
   @Override
   public <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> function, @Nullable Runnable retired, long delay, long period) {
      FoliaTask<T> taskImplementation = new FoliaTask<>();
      Consumer<ScheduledTask> foliaConsumer = this.buildFoliaConsumer(taskImplementation, function);
      ScheduledTask scheduledTask = this.entityScheduler.runAtFixedRate(this.plugin, foliaConsumer, retired, delay, period);
      if (scheduledTask == null) {
         return null;
      }

      taskImplementation.setScheduledTask(scheduledTask);
      return taskImplementation;
   }
}
