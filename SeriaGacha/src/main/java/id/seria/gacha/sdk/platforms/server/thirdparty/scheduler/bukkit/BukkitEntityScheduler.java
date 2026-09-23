package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.bukkit;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.EntitySchedulerImplementation;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import java.util.function.Function;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class BukkitEntityScheduler implements EntitySchedulerImplementation {
   @NotNull
   private final Plugin plugin;
   @NotNull
   private final Entity entity;

   @Internal
   public BukkitEntityScheduler(@NotNull Plugin plugin, @NotNull Entity entity) {
      this.plugin = plugin;
      this.entity = entity;
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

   private boolean isRetired(@Nullable Runnable retired) {
      if (!this.entity.isValid()) {
         if (retired != null) {
            retired.run();
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean execute(@NotNull final Runnable run, @Nullable final Runnable retired, long delay) {
      if (this.isRetired(retired)) {
         return false;
      }

      (new BukkitRunnable() {
         public void run() {
            if (!BukkitEntityScheduler.this.isRetired(retired)) {
               run.run();
            }
         }
      }).runTaskLater(this.plugin, delay);
      return true;
   }

   @Nullable
   @Override
   public <T> TaskImplementation<T> run(@NotNull Function<TaskImplementation<T>, T> function, @Nullable Runnable retired) {
      if (this.isRetired(retired)) {
         return null;
      }

      BukkitTask<T> taskImplementation = new BukkitTask<>(this.plugin, false);
      BukkitRunnable runnable = this.buildBukkitRunnable(function, taskImplementation);
      taskImplementation.setScheduledTask(runnable.runTask(this.plugin));
      return taskImplementation;
   }

   @Nullable
   @Override
   public <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> function, @Nullable Runnable retired, long delay) {
      if (this.isRetired(retired)) {
         return null;
      }

      BukkitTask<T> taskImplementation = new BukkitTask<>(this.plugin, false);
      BukkitRunnable runnable = this.buildBukkitRunnable(function, taskImplementation);
      taskImplementation.setScheduledTask(runnable.runTaskLater(this.plugin, delay));
      return taskImplementation;
   }

   @Nullable
   @Override
   public <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> function, @Nullable Runnable retired, long delay, long period) {
      if (this.isRetired(retired)) {
         return null;
      }

      BukkitTask<T> taskImplementation = new BukkitTask<>(this.plugin, true);
      BukkitRunnable runnable = this.buildBukkitRunnable(function, taskImplementation);
      taskImplementation.setScheduledTask(runnable.runTaskTimer(this.plugin, delay, period));
      return taskImplementation;
   }
}
