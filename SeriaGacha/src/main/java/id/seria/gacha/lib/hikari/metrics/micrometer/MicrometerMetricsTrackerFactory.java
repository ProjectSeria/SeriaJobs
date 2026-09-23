package id.seria.gacha.lib.hikari.metrics.micrometer;

import id.seria.gacha.lib.hikari.metrics.IMetricsTracker;
import id.seria.gacha.lib.hikari.metrics.MetricsTrackerFactory;
import id.seria.gacha.lib.hikari.metrics.PoolStats;
import io.micrometer.core.instrument.MeterRegistry;

public class MicrometerMetricsTrackerFactory implements MetricsTrackerFactory {
   private final MeterRegistry registry;

   public MicrometerMetricsTrackerFactory(MeterRegistry registry) {
      this.registry = registry;
   }

   @Override
   public IMetricsTracker create(String poolName, PoolStats poolStats) {
      return new MicrometerMetricsTracker(poolName, poolStats, this.registry);
   }
}
