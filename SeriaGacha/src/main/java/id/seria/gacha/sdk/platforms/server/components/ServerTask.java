package id.seria.gacha.sdk.platforms.server.components;

import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.TaskImplementation;
import org.jetbrains.annotations.Nullable;

public class ServerTask implements Task {
   private final TaskImplementation<Void> task;

   public ServerTask(@Nullable TaskImplementation<Void> task) {
      this.task = task;
   }

   @Override
   public void cancel() {
      if (this.task != null) {
         this.task.cancel();
      }
   }
}
