package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.bukkit;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.AsyncSchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class BukkitAsyncScheduler implements AsyncSchedulerImplementation {
   @NotNull
   private final Plugin plugin;

   public BukkitAsyncScheduler(@NotNull Plugin plugin) {
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

   @NotNull
   @Override
   public <T> TaskImplementation<T> runNow(@NotNull Function<TaskImplementation<T>, T> function) {
      BukkitTask<T> taskImplementation = new BukkitTask<>(this.plugin, false);
      BukkitRunnable runnable = this.buildBukkitRunnable(function, taskImplementation);
      taskImplementation.setScheduledTask(runnable.runTaskAsynchronously(this.plugin));
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> function, long delay, @NotNull TimeUnit unit) {
      BukkitTask<T> taskImplementation = new BukkitTask<>(this.plugin, false);
      BukkitRunnable runnable = this.buildBukkitRunnable(function, taskImplementation);
      taskImplementation.setScheduledTask(runnable.runTaskLaterAsynchronously(this.plugin, unit.toSeconds(delay) * 20L));
      return taskImplementation;
   }

   @NotNull
   @Override
   public <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> function, long delay, long period, @NotNull TimeUnit unit) {
      BukkitTask<T> taskImplementation = new BukkitTask<>(this.plugin, true);
      BukkitRunnable runnable = this.buildBukkitRunnable(function, taskImplementation);
      taskImplementation.setScheduledTask(runnable.runTaskTimerAsynchronously(this.plugin, unit.toSeconds(delay) * 20L, unit.toSeconds(period) * 20L));
      return taskImplementation;
   }

   @Override
   public void cancelTasks() {
      this.plugin.getServer().getScheduler().cancelTasks(this.plugin);
   }
}
