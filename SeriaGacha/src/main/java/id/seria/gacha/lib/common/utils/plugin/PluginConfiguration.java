package id.seria.gacha.lib.common.utils.plugin;

import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import lombok.Generated;

public abstract class PluginConfiguration extends Configuration {
   @ConfigField
   protected String productLicense;

   public PluginConfiguration() {
   }

   @Generated
   public String getProductLicense() {
      return this.productLicense;
   }
}
