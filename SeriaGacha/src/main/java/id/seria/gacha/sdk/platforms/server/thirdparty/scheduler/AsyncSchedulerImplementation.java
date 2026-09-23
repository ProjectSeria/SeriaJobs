package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AsyncSchedulerImplementation {
   @NotNull
   <T> TaskImplementation<T> runNow(@NotNull Function<TaskImplementation<T>, T> var1);

   @NotNull
   default TaskImplementation<Void> runNow(@NotNull Consumer<TaskImplementation<Void>> consumer) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         consumer.accept(task);
         return null;
      };
      return this.runNow(wrapperFunction);
   }

   @NotNull
   default TaskImplementation<Void> runNow(@NotNull Runnable runnable) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         runnable.run();
         return null;
      };
      return this.runNow(wrapperFunction);
   }

   @NotNull
   <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> var1, long var2, @NotNull TimeUnit var4);

   @NotNull
   default TaskImplementation<Void> runDelayed(@NotNull Consumer<TaskImplementation<Void>> consumer, long delay, @NotNull TimeUnit unit) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         consumer.accept(task);
         return null;
      };
      return this.runDelayed(wrapperFunction, delay, unit);
   }

   @NotNull
   default TaskImplementation<Void> runDelayed(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         runnable.run();
         return null;
      };
      return this.runDelayed(wrapperFunction, delay, unit);
   }

   @NotNull
   default <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, @Nullable T> function, long ticks) {
      return this.runDelayed(function, ticks * 50L, TimeUnit.MILLISECONDS);
   }

   @NotNull
   default TaskImplementation<Void> runDelayed(@NotNull Consumer<TaskImplementation<Void>> consumer, long ticks) {
      return this.runDelayed(consumer, ticks * 50L, TimeUnit.MILLISECONDS);
   }

   @NotNull
   default TaskImplementation<Void> runDelayed(@NotNull Runnable runnable, long ticks) {
      return this.runDelayed(runnable, ticks * 50L, TimeUnit.MILLISECONDS);
   }

   @NotNull
   <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> var1, long var2, long var4, @NotNull TimeUnit var6);

   @NotNull
   default TaskImplementation<Void> runAtFixedRate(@NotNull Consumer<TaskImplementation<Void>> consumer, long delay, long period, @NotNull TimeUnit unit) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         consumer.accept(task);
         return null;
      };
      return this.runAtFixedRate(wrapperFunction, delay, period, unit);
   }

   @NotNull
   default TaskImplementation<Void> runAtFixedRate(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         runnable.run();
         return null;
      };
      return this.runAtFixedRate(wrapperFunction, delay, period, unit);
   }

   @NotNull
   default <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, @Nullable T> function, long delay, long ticks) {
      return this.runAtFixedRate(function, delay * 50L, ticks * 50L, TimeUnit.MILLISECONDS);
   }

   @NotNull
   default TaskImplementation<Void> runAtFixedRate(@NotNull Consumer<TaskImplementation<Void>> consumer, long delay, long ticks) {
      return this.runAtFixedRate(consumer, delay * 50L, ticks * 50L, TimeUnit.MILLISECONDS);
   }

   @NotNull
   default TaskImplementation<Void> runAtFixedRate(@NotNull Runnable runnable, long delay, long ticks) {
      return this.runAtFixedRate(runnable, delay * 50L, ticks * 50L, TimeUnit.MILLISECONDS);
   }

   void cancelTasks();
}
