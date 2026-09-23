package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler;

import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntitySchedulerImplementation {
   boolean execute(@NotNull Runnable var1, @Nullable Runnable var2, long var3);

   default boolean execute(@NotNull Runnable run) {
      return this.execute(run, null, 1L);
   }

   @Nullable
   <T> TaskImplementation<T> run(@NotNull Function<TaskImplementation<T>, T> var1, @Nullable Runnable var2);

   @Nullable
   default TaskImplementation<Void> run(@NotNull Consumer<TaskImplementation<Void>> consumer, @Nullable Runnable retired) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         consumer.accept(task);
         return null;
      };
      return this.run(wrapperFunction, retired);
   }

   @Nullable
   default TaskImplementation<Void> run(@NotNull Runnable runnable, @Nullable Runnable retired) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         runnable.run();
         return null;
      };
      return this.run(wrapperFunction, retired);
   }

   @Nullable
   default <T> TaskImplementation<T> run(@NotNull Function<TaskImplementation<T>, T> function) {
      return this.run(function, null);
   }

   @Nullable
   default TaskImplementation<Void> run(@NotNull Consumer<TaskImplementation<Void>> consumer) {
      return this.run(consumer, null);
   }

   @Nullable
   default TaskImplementation<Void> run(@NotNull Runnable runnable) {
      return this.run(runnable, null);
   }

   @Nullable
   <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> var1, @Nullable Runnable var2, long var3);

   @Nullable
   default TaskImplementation<Void> runDelayed(@NotNull Consumer<TaskImplementation<Void>> consumer, @Nullable Runnable retired, long delay) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         consumer.accept(task);
         return null;
      };
      return this.runDelayed(wrapperFunction, retired, delay);
   }

   @Nullable
   default TaskImplementation<Void> runDelayed(@NotNull Runnable runnable, @Nullable Runnable retired, long delay) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         runnable.run();
         return null;
      };
      return this.runDelayed(wrapperFunction, retired, delay);
   }

   @Nullable
   default <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> function, long delay) {
      return this.runDelayed(function, null, delay);
   }

   @Nullable
   default TaskImplementation<Void> runDelayed(@NotNull Consumer<TaskImplementation<Void>> consumer, long delay) {
      return this.runDelayed(consumer, null, delay);
   }

   @Nullable
   default TaskImplementation<Void> runDelayed(@NotNull Runnable runnable, long delay) {
      return this.runDelayed(runnable, null, delay);
   }

   @Nullable
   <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> var1, @Nullable Runnable var2, long var3, long var5);

   @Nullable
   default TaskImplementation<Void> runAtFixedRate(@NotNull Consumer<TaskImplementation<Void>> consumer, @Nullable Runnable retired, long delay, long period) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         consumer.accept(task);
         return null;
      };
      return this.runAtFixedRate(wrapperFunction, retired, delay, period);
   }

   @Nullable
   default TaskImplementation<Void> runAtFixedRate(@NotNull Runnable runnable, @Nullable Runnable retired, long delay, long period) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         runnable.run();
         return null;
      };
      return this.runAtFixedRate(wrapperFunction, retired, delay, period);
   }

   @Nullable
   default <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> function, long delay, long period) {
      return this.runAtFixedRate(function, null, delay, period);
   }

   @Nullable
   default TaskImplementation<Void> runAtFixedRate(@NotNull Consumer<TaskImplementation<Void>> consumer, long delay, long period) {
      return this.runAtFixedRate(consumer, null, delay, period);
   }

   @Nullable
   default TaskImplementation<Void> runAtFixedRate(@NotNull Runnable runnable, long delay, long period) {
      return this.runAtFixedRate(runnable, null, delay, period);
   }
}
