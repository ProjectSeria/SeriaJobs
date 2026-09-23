package id.seria.gacha.configurations.crates;

import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Generated;

public class CratesLocationsConfiguration extends Configuration {
   @ConfigField
   private LinkedHashMap<String, List<CrateLocation>> locations = new LinkedHashMap<>();

   @Override
   public Configuration clone() {
      return new CratesLocationsConfiguration();
   }

   @Generated
   public LinkedHashMap<String, List<CrateLocation>> getLocations() {
      return this.locations;
   }

   @Generated
   public CratesLocationsConfiguration() {
   }
}
