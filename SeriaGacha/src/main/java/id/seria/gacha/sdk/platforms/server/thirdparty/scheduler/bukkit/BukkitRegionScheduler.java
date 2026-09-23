package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.bukkit;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.RegionSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import java.util.function.Function;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BukkitRegionScheduler implements RegionSchedulerImplementation {
   @NotNull
   private final Plugin plugin;

   public BukkitRegionScheduler(@NotNull Plugin plugin) {
      this.plugin = plugin;
   }

   @NotNull
   private <T> BukkitRunnable buildBukkitRunnable(@NotNull final Function<TaskImplementation<T>, T> function, @NotNull final BukkitTask<T> taskImplementation) {
      return new BukkitRunnable() {
         public void run() {
            taskImplementation.setCallback(function.apply(taskImplementation));
            taskImplementation.asFuture().complete(taskImplementation);
         }
      };
   }

   @Override
   public void execute(@NotNull final Runnable run) {
      (new BukkitRunnable() {
         public void run() {
            run.run();
         }
      }).runTask(this.plugin);
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> run(@NotNull Function<TaskImplementation<T>, T> function) {
      BukkitTask<T> taskImplementation = new BukkitTask<>(this.plugin, false);
      BukkitRunnable runnable = this.buildBukkitRunnable(function, taskImplementation);
      taskImplementation.setScheduledTask(runnable.runTask(this.plugin));
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> function, long delay) {
      BukkitTask<T> taskImplementation = new BukkitTask<>(this.plugin, false);
      BukkitRunnable runnable = this.buildBukkitRunnable(function, taskImplementation);
      taskImplementation.setScheduledTask(runnable.runTaskLater(this.plugin, delay));
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> function, long delay, long period) {
      BukkitTask<T> taskImplementation = new BukkitTask<>(this.plugin, true);
      BukkitRunnable runnable = this.buildBukkitRunnable(function, taskImplementation);
      taskImplementation.setScheduledTask(runnable.runTaskTimer(this.plugin, delay, period));
      return taskImplementation;
   }
}
