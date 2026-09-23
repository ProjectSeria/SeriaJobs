package id.seria.gacha.api.sdk.config;

public interface PluginConfiguration {
   DatabaseConfiguration getDatabase();

   int getInternalPoolSize();
}
