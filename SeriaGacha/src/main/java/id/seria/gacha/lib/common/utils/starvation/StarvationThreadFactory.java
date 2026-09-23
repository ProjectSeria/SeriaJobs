package id.seria.gacha.lib.common.utils.starvation;

import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class StarvationThreadFactory implements ThreadFactory {
   private final ServerPlugin plugin;
   private final AtomicLong counter = new AtomicLong(0L);

   public StarvationThreadFactory(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public Thread newThread(Runnable r) {
      Thread thread = new Thread(r);
      thread.setName(this.plugin.getName() + "-Async-" + this.counter.incrementAndGet());
      thread.setUncaughtExceptionHandler((t, e) -> this.plugin.getLogger().error("Uncaught exception in thread " + t.getName(), e));
      if (thread.isDaemon()) {
         thread.setDaemon(false);
      }

      if (thread.getPriority() != 5) {
         thread.setPriority(5);
      }

      return thread;
   }
}
