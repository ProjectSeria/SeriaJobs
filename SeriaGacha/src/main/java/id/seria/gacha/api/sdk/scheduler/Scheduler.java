package id.seria.gacha.api.sdk.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface Scheduler {
   Task runSync(Runnable var1);

   Task runSync(Runnable var1, long var2);

   Task runAsync(Runnable var1);

   Task runAsync(Runnable var1, long var2);

   Task scheduleSync(Runnable var1, long var2, long var4);

   Task scheduleAsync(Runnable var1, long var2, long var4);

   <T> Future<T> callSyncMethod(Callable<T> var1);
}
