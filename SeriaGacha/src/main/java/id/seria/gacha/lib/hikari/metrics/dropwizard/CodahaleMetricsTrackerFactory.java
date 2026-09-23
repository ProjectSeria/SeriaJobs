package id.seria.gacha.lib.hikari.metrics.dropwizard;

import com.codahale.metrics.MetricRegistry;
import id.seria.gacha.lib.hikari.metrics.IMetricsTracker;
import id.seria.gacha.lib.hikari.metrics.MetricsTrackerFactory;
import id.seria.gacha.lib.hikari.metrics.PoolStats;

public final class CodahaleMetricsTrackerFactory implements MetricsTrackerFactory {
   private final MetricRegistry registry;

   public CodahaleMetricsTrackerFactory(MetricRegistry registry) {
      this.registry = registry;
   }

   public MetricRegistry getRegistry() {
      return this.registry;
   }

   @Override
   public IMetricsTracker create(String poolName, PoolStats poolStats) {
      return new CodaHaleMetricsTracker(poolName, poolStats, this.registry);
   }
}
