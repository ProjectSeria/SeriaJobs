package id.seria.gacha.api.sdk.config;

import java.io.File;

public interface YamlConfiguration extends ConfigurationSection {
   void load(File var1);

   void save(File var1);
}
