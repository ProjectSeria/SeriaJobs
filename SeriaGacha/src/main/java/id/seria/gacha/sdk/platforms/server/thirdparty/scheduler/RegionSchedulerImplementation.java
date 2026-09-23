package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler;

import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public interface RegionSchedulerImplementation {
   void execute(@NotNull Runnable var1);

   @NotNull
   <T> TaskImplementation<T> run(@NotNull Function<TaskImplementation<T>, T> var1);

   @NotNull
   default TaskImplementation<Void> run(@NotNull Consumer<TaskImplementation<Void>> consumer) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         consumer.accept(task);
         return null;
      };
      return this.run(wrapperFunction);
   }

   @NotNull
   default TaskImplementation<Void> run(@NotNull Runnable runnable) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         runnable.run();
         return null;
      };
      return this.run(wrapperFunction);
   }

   @NotNull
   <T> TaskImplementation<T> runDelayed(@NotNull Function<TaskImplementation<T>, T> var1, long var2);

   @NotNull
   default TaskImplementation<Void> runDelayed(@NotNull Consumer<TaskImplementation<Void>> consumer, long delay) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         consumer.accept(task);
         return null;
      };
      return this.runDelayed(wrapperFunction, delay);
   }

   @NotNull
   default TaskImplementation<Void> runDelayed(@NotNull Runnable runnable, long delay) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         runnable.run();
         return null;
      };
      return this.runDelayed(wrapperFunction, delay);
   }

   @NotNull
   <T> TaskImplementation<T> runAtFixedRate(@NotNull Function<TaskImplementation<T>, T> var1, long var2, long var4);

   @NotNull
   default TaskImplementation<Void> runAtFixedRate(@NotNull Consumer<TaskImplementation<Void>> consumer, long delay, long period) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         consumer.accept(task);
         return null;
      };
      return this.runAtFixedRate(wrapperFunction, delay, period);
   }

   @NotNull
   default TaskImplementation<Void> runAtFixedRate(@NotNull Runnable runnable, long delay, long period) {
      Function<TaskImplementation<Void>, Void> wrapperFunction = task -> {
         runnable.run();
         return null;
      };
      return this.runAtFixedRate(wrapperFunction, delay, period);
   }
}
