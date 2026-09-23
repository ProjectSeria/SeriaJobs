package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler;

import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TaskImplementation<T> {
   @NotNull
   Plugin getOwningPlugin();

   void cancel();

   boolean isCancelled();

   boolean isRunning();

   boolean isRepeatingTask();

   @Nullable
   T getCallback();

   @NotNull
   CompletableFuture<TaskImplementation<T>> asFuture();
}
