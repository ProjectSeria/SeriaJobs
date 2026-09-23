package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.bukkit;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class BukkitTask<T> implements TaskImplementation<T> {
   @NotNull
   private final Plugin owningPlugin;
   private final boolean isRepeatingTask;
   @NotNull
   private final ReentrantLock lock;
   @NotNull
   private final AtomicReference<org.bukkit.scheduler.BukkitTask> scheduledTaskRef;
   @NotNull
   private final CompletableFuture<TaskImplementation<T>> future;
   @Nullable
   private T callback;

   @Internal
   public BukkitTask(@NotNull Plugin owningPlugin, boolean isRepeatingTask) {
      this.owningPlugin = owningPlugin;
      this.isRepeatingTask = isRepeatingTask;
      this.lock = new ReentrantLock();
      this.scheduledTaskRef = new AtomicReference<>();
      this.future = new CompletableFuture<>();
   }

   @Internal
   public void setScheduledTask(@NotNull org.bukkit.scheduler.BukkitTask task) {
      this.scheduledTaskRef.set(task);
   }

   @NotNull
   @Override
   public Plugin getOwningPlugin() {
      return this.owningPlugin;
   }

   @Override
   public void cancel() {
      this.scheduledTaskRef.get().cancel();
   }

   @Override
   public boolean isCancelled() {
      return this.scheduledTaskRef.get().isCancelled();
   }

   @Override
   public boolean isRunning() {
      return this.owningPlugin.getServer().getScheduler().isCurrentlyRunning(this.scheduledTaskRef.get().getTaskId());
   }

   @Override
   public boolean isRepeatingTask() {
      return this.isRepeatingTask;
   }

   @Nullable
   @Override
   public T getCallback() {
      this.lock.lock();

      try {
         return this.callback;
      } finally {
         this.lock.unlock();
      }
   }

   @Internal
   public void setCallback(@Nullable T callback) {
      this.lock.lock();

      try {
         this.callback = callback;
      } finally {
         this.lock.unlock();
      }
   }

   @NotNull
   @Override
   public CompletableFuture<TaskImplementation<T>> asFuture() {
      return this.future;
   }
}
