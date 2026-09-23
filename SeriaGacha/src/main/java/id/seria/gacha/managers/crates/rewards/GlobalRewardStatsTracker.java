package id.seria.gacha.managers.crates.rewards;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.lib.common.services.services.database.context.DatabaseRepository;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalRewardStatsTracker {
   private final PhoenixCrates plugin;
   private DatabaseRepository<RewardGlobalStatsModel> databaseRepository;
   private final Map<String, GlobalRewardStatsTracker.CachedStat> cache = new ConcurrentHashMap<>();
   private Task flushTask;

   public GlobalRewardStatsTracker(PhoenixCrates plugin) {
      this.plugin = plugin;
   }

   public void load() {
      this.databaseRepository = new DatabaseRepository<>(this.plugin.getDatabaseProvider(), RewardGlobalStatsModel.class);

      for (RewardGlobalStatsModel model : this.databaseRepository.fetchAllNow()) {
         this.cache
            .put(model.getCompositeIdentifier(), new GlobalRewardStatsTracker.CachedStat(model.getId(), model.getWinAmount(), model.getLastWinTimestamp()));
      }

      this.flushTask = Crates.getScheduler().scheduleAsync(this::flushDirtyEntries, 100L, 100L);
   }

   public void unload() {
      if (this.flushTask != null) {
         this.flushTask.cancel();
         this.flushTask = null;
      }

      this.flushDirtyEntries();
   }

   public int getWinAmount(String crateIdentifier, String rewardIdentifier) {
      GlobalRewardStatsTracker.CachedStat stat = this.cache.get(compositeKey(crateIdentifier, rewardIdentifier));
      return stat == null ? 0 : stat.winAmount;
   }

   public long getLastWinTimestamp(String crateIdentifier, String rewardIdentifier) {
      GlobalRewardStatsTracker.CachedStat stat = this.cache.get(compositeKey(crateIdentifier, rewardIdentifier));
      return stat == null ? 0L : stat.lastWinTimestamp;
   }

   public void incrementWinAmount(String crateIdentifier, String rewardIdentifier) {
      this.cache.computeIfAbsent(compositeKey(crateIdentifier, rewardIdentifier), key -> new GlobalRewardStatsTracker.CachedStat(-1, 0, 0L)).increment();
   }

   public void markRewardWon(String crateIdentifier, String rewardIdentifier) {
      this.cache.computeIfAbsent(compositeKey(crateIdentifier, rewardIdentifier), key -> new GlobalRewardStatsTracker.CachedStat(-1, 0, 0L)).touch();
   }

   public boolean resetWinAmount(String crateIdentifier, String rewardIdentifier) {
      GlobalRewardStatsTracker.CachedStat stat = this.cache.get(compositeKey(crateIdentifier, rewardIdentifier));
      if (stat == null) {
         return false;
      }

      stat.reset();
      return true;
   }

   public boolean resetCooldown(String crateIdentifier, String rewardIdentifier) {
      GlobalRewardStatsTracker.CachedStat stat = this.cache.get(compositeKey(crateIdentifier, rewardIdentifier));
      if (stat == null) {
         return false;
      }

      stat.resetCooldown();
      return true;
   }

   private static String compositeKey(String crateIdentifier, String rewardIdentifier) {
      return crateIdentifier + ":" + rewardIdentifier;
   }

   private void flushDirtyEntries() {
      for (Entry<String, GlobalRewardStatsTracker.CachedStat> entry : this.cache.entrySet()) {
         GlobalRewardStatsTracker.CachedStat stat = entry.getValue();
         if (!stat.inserting && stat.dirty) {
            int winAmount;
            long lastWinTimestamp;
            synchronized (stat.lock) {
               winAmount = stat.winAmount;
               lastWinTimestamp = stat.lastWinTimestamp;
               stat.dirty = false;
            }

            String compositeIdentifier = entry.getKey();
            int separatorIndex = compositeIdentifier.indexOf(58);
            String crateIdentifier = compositeIdentifier.substring(0, separatorIndex);
            String rewardIdentifier = compositeIdentifier.substring(separatorIndex + 1);
            if (stat.databaseId <= 0) {
               stat.inserting = true;
               RewardGlobalStatsModel model = new RewardGlobalStatsModel(
                  new Object(), -1, compositeIdentifier, crateIdentifier, rewardIdentifier, winAmount, lastWinTimestamp
               );
               this.databaseRepository.insertLater(model).whenComplete((result, throwable) -> {
                  if (Boolean.TRUE.equals(result)) {
                     stat.databaseId = model.getId();
                  } else {
                     stat.dirty = true;
                  }

                  stat.inserting = false;
               });
            } else {
               RewardGlobalStatsModel model = new RewardGlobalStatsModel(
                  new Object(), stat.databaseId, compositeIdentifier, crateIdentifier, rewardIdentifier, winAmount, lastWinTimestamp
               );
               this.databaseRepository.updateLater(model);
            }
         }
      }
   }

   private static final class CachedStat {
      private final Object lock = new Object();
      private volatile int databaseId;
      private volatile int winAmount;
      private volatile long lastWinTimestamp;
      private volatile boolean dirty;
      private volatile boolean inserting;

      private CachedStat(int databaseId, int winAmount, long lastWinTimestamp) {
         this.databaseId = databaseId;
         this.winAmount = winAmount;
         this.lastWinTimestamp = lastWinTimestamp;
      }

      private void increment() {
         synchronized (this.lock) {
            this.winAmount++;
            this.dirty = true;
         }
      }

      private void touch() {
         synchronized (this.lock) {
            this.lastWinTimestamp = System.currentTimeMillis();
            this.dirty = true;
         }
      }

      private void reset() {
         synchronized (this.lock) {
            this.winAmount = 0;
            this.dirty = true;
         }
      }

      private void resetCooldown() {
         synchronized (this.lock) {
            this.lastWinTimestamp = 0L;
            this.dirty = true;
         }
      }
   }
}
