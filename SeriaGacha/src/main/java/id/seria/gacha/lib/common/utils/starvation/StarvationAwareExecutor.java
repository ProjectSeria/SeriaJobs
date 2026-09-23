package id.seria.gacha.lib.common.utils.starvation;

import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.Generated;

public class StarvationAwareExecutor implements ExecutorService {
   private final ServerPlugin plugin;
   private final ThreadPoolExecutor executor;
   private final ExecutorService timeoutEnforcer;
   private final long timeoutMillis;
   private final int queueWarnThreshold;

   public StarvationAwareExecutor(ServerPlugin plugin, int threadPoolSize, long timeoutMillis, int starvationMultiplier) {
      this.plugin = plugin;
      int actualPoolSize = Math.max(3, threadPoolSize);
      this.timeoutMillis = timeoutMillis;
      this.queueWarnThreshold = actualPoolSize * starvationMultiplier;
      BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
      this.executor = new ThreadPoolExecutor(actualPoolSize, actualPoolSize, 0L, TimeUnit.MILLISECONDS, queue, new StarvationThreadFactory(plugin));
      this.timeoutEnforcer = Executors.newCachedThreadPool(new StarvationThreadFactory(plugin));
   }

   @Override
   public void execute(Runnable task) {
      int queueSize = this.executor.getQueue().size();
      if (queueSize > this.queueWarnThreshold) {
         this.plugin
            .getLogger()
            .warn("[StarvationAwareExecutor] Queue size (" + queueSize + ") exceeds threshold (" + this.queueWarnThreshold + "). Possible starvation.");
      }

      Future<?> future = this.executor.submit(task);
      this.timeoutEnforcer.execute(() -> {
         try {
            future.get(this.timeoutMillis, TimeUnit.MILLISECONDS);
         } catch (TimeoutException e) {
            this.plugin.getLogger().warn("[StarvationAwareExecutor] Task exceeded timeout of " + this.timeoutMillis + "ms");
            future.cancel(true);
         } catch (Exception e) {
            this.plugin.getLogger().error("[StarvationAwareExecutor] Task execution error", e);
         }
      });
   }

   @Override
   public void shutdown() {
      this.executor.shutdown();
      this.timeoutEnforcer.shutdown();
   }

   @Override
   public List<Runnable> shutdownNow() {
      List<Runnable> a = this.executor.shutdownNow();
      List<Runnable> b = this.timeoutEnforcer.shutdownNow();
      a.addAll(b);
      return a;
   }

   @Override
   public boolean isShutdown() {
      return this.executor.isShutdown() && this.timeoutEnforcer.isShutdown();
   }

   @Override
   public boolean isTerminated() {
      return this.executor.isTerminated() && this.timeoutEnforcer.isTerminated();
   }

   @Override
   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return this.executor.awaitTermination(timeout, unit) && this.timeoutEnforcer.awaitTermination(timeout, unit);
   }

   @Override
   public <T> Future<T> submit(Callable<T> task) {
      FutureTask<T> ft = new FutureTask<>(task);
      this.execute(ft);
      return ft;
   }

   @Override
   public <T> Future<T> submit(Runnable task, T result) {
      FutureTask<T> ft = new FutureTask<>(task, result);
      this.execute(ft);
      return ft;
   }

   @Override
   public Future<?> submit(Runnable task) {
      FutureTask<?> ft = new FutureTask(task, null);
      this.execute(ft);
      return ft;
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
      throw new UnsupportedOperationException();
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
      throw new UnsupportedOperationException();
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
      throw new UnsupportedOperationException();
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      throw new UnsupportedOperationException();
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public ThreadPoolExecutor getExecutor() {
      return this.executor;
   }

   @Generated
   public ExecutorService getTimeoutEnforcer() {
      return this.timeoutEnforcer;
   }

   @Generated
   public long getTimeoutMillis() {
      return this.timeoutMillis;
   }

   @Generated
   public int getQueueWarnThreshold() {
      return this.queueWarnThreshold;
   }
}
