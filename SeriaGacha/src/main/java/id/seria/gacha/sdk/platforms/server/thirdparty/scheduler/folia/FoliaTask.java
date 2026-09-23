package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.folia;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class FoliaTask<T> implements TaskImplementation<T> {
   private final ReentrantLock lock = new ReentrantLock();
   private final AtomicReference<ScheduledTask> scheduledTaskRef = new AtomicReference<>();
   private final CompletableFuture<TaskImplementation<T>> future = new CompletableFuture<>();
   private T callback;

   @Internal
   public FoliaTask() {
   }

   @Internal
   public void setScheduledTask(@NotNull ScheduledTask task) {
      this.scheduledTaskRef.set(task);
   }

   @NotNull
   @Override
   public Plugin getOwningPlugin() {
      return this.scheduledTaskRef.get().getOwningPlugin();
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
      ExecutionState state = this.scheduledTaskRef.get().getExecutionState();
      return state == ExecutionState.RUNNING || state == ExecutionState.CANCELLED_RUNNING;
   }

   @Override
   public boolean isRepeatingTask() {
      return this.scheduledTaskRef.get().isRepeatingTask();
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
   public void setCallback(T callback) {
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
