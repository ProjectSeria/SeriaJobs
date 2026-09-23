package id.seria.gacha.api.sdk.config;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

public interface ConfigurationProvider {
   YamlConfiguration load(File var1);

   YamlConfiguration load(InputStream var1);

   YamlConfiguration load(Reader var1);
}
