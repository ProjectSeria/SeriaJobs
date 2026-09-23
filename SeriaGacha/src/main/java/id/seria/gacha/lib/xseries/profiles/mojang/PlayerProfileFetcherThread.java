package id.seria.gacha.lib.xseries.profiles.mojang;

import id.seria.gacha.lib.xseries.profiles.ProfileLogger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PlayerProfileFetcherThread implements ThreadFactory {
   public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10, new PlayerProfileFetcherThread());
   private static final AtomicInteger COUNT = new AtomicInteger();

   public PlayerProfileFetcherThread() {
   }

   @Override
   public Thread newThread(@NotNull Runnable run) {
      Thread thread = new Thread(run);
      thread.setName("Profile Lookup Executor #" + COUNT.getAndIncrement());
      thread.setUncaughtExceptionHandler((t, throwable) -> ProfileLogger.LOGGER.error("Uncaught exception in thread {}", t.getName(), throwable));
      return thread;
   }
}
