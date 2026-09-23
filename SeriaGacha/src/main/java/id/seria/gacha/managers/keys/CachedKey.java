package id.seria.gacha.managers.keys;

import id.seria.gacha.lib.common.utils.configuration.workers.ConfigurationWorker;
import lombok.Generated;

public class CachedKey {
   private final ConfigurationWorker configurationWorker;
   private final Key key;

   public CachedKey(ConfigurationWorker configurationWorker, Key key) {
      this.configurationWorker = configurationWorker;
      this.key = key;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof CachedKey && ((CachedKey)object).getKey().equals(this.key);
   }

   @Override
   public int hashCode() {
      return this.key.hashCode();
   }

   @Generated
   public ConfigurationWorker getConfigurationWorker() {
      return this.configurationWorker;
   }

   @Generated
   public Key getKey() {
      return this.key;
   }
}
