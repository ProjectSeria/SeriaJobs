package id.seria.gacha.limbo.debug;

import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DebugExecutorService implements ExecutorService {
   private final ExecutorService delegate;
   private final ServerPlugin plugin;
   private final Logger debugLogger = Logger.getLogger("DebugExecutorLogger");
   private FileHandler fileHandler;
   private final File debugOutputFolder;
   private final File globalDebugLogFile;
   private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   public DebugExecutorService(ServerPlugin plugin, ExecutorService delegate) {
      this.plugin = plugin;
      this.delegate = delegate;
      this.debugOutputFolder = new File(plugin.getDataFolder(), "debug_output");
      this.globalDebugLogFile = new File(plugin.getDataFolder(), "debug.log");

      try {
         this.initDebugFolder();
         if (this.fileHandler == null) {
            this.fileHandler = new FileHandler(this.globalDebugLogFile.getAbsolutePath(), true);
            this.fileHandler.setFormatter(new SimpleFormatter());
            this.debugLogger.addHandler(this.fileHandler);
         }
      } catch (IOException e) {
         plugin.getLogger().warn("Could not create debug.log file handler: " + e.getMessage());
      }
   }

   private void initDebugFolder() {
      this.plugin.getLogger().warn("Init debug folder");
      if (!this.debugOutputFolder.exists()) {
         this.debugOutputFolder.mkdirs();
      }
   }

   private synchronized void appendToGlobalLog(String message) {
      try {
         FileWriter fw = new FileWriter(this.globalDebugLogFile, true);
         fw.write(message);
         fw.close();
      } catch (IOException e) {
         this.plugin.getLogger().warn("Failed to write to global debug log: " + e.getMessage());
      }
   }

   private synchronized void appendToInstanceLog(String instanceId, String message) {
      File instanceLogFile = new File(this.debugOutputFolder, instanceId + "-debug.log");

      try {
         FileWriter fw = new FileWriter(instanceLogFile, true);
         fw.write(message);
         fw.close();
      } catch (IOException e) {
         this.plugin.getLogger().warn("Failed to write to instance debug log: " + e.getMessage());
      }
   }

   private String getStackTrace() {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();

      for (int i = 3; i < stack.length; i++) {
         pw.println("\tat " + stack[i].toString());
      }

      return sw.toString();
   }

   @Override
   public void execute(final Runnable command) {
      final String instanceId = UUID.randomUUID().toString();
      this.debugLogger.info("Async service requested with ID: " + instanceId);
      final String startTimestamp = this.timestampFormat.format(new Date());
      this.appendToInstanceLog(instanceId, startTimestamp + " - Executor async going to be executed\n");
      this.appendToInstanceLog(instanceId, this.getStackTrace());
      this.delegate.execute(new Runnable() {
         @Override
         public void run() {
            long startTime = System.nanoTime();
            DebugExecutorService.this.appendToInstanceLog(instanceId, startTimestamp + " - Executor async executing...\n");

            try {
               command.run();
            } finally {
               long durationMs = (System.nanoTime() - startTime) / 1000000L;
               String endTimestamp = DebugExecutorService.this.timestampFormat.format(new Date());
               DebugExecutorService.this.appendToInstanceLog(instanceId, endTimestamp + " - Executor async executed in " + durationMs + " ms\n\n");
            }
         }
      });
   }

   @Override
   public void shutdown() {
      this.delegate.shutdown();
   }

   @Override
   public List<Runnable> shutdownNow() {
      return this.delegate.shutdownNow();
   }

   @Override
   public boolean isShutdown() {
      return this.delegate.isShutdown();
   }

   @Override
   public boolean isTerminated() {
      return this.delegate.isTerminated();
   }

   @Override
   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate.awaitTermination(timeout, unit);
   }

   @Override
   public <T> Future<T> submit(Callable<T> task) {
      return this.delegate.submit(task);
   }

   @Override
   public <T> Future<T> submit(Runnable task, T result) {
      return this.delegate.submit(task, result);
   }

   @Override
   public Future<?> submit(Runnable task) {
      return this.delegate.submit(task);
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
      return this.delegate.invokeAll(tasks);
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
      return this.delegate.invokeAll(tasks, timeout, unit);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
      return this.delegate.invokeAny(tasks);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return this.delegate.invokeAny(tasks, timeout, unit);
   }
}
