package id.seria.gacha.managers.crates.type;

import id.seria.gacha.lib.common.utils.configuration.workers.ConfigurationWorker;
import id.seria.gacha.managers.crates.CrateType;
import lombok.Generated;

public class CachedCrateType {
   private final ConfigurationWorker configurationWorker;
   private final CrateType type;

   public CachedCrateType(ConfigurationWorker configurationWorker, CrateType type) {
      this.configurationWorker = configurationWorker;
      this.type = type;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof CachedCrateType && ((CachedCrateType)object).getType().equals(this.type);
   }

   @Override
   public int hashCode() {
      return this.type.hashCode();
   }

   @Generated
   public ConfigurationWorker getConfigurationWorker() {
      return this.configurationWorker;
   }

   @Generated
   public CrateType getType() {
      return this.type;
   }
}
