package id.seria.gacha.sdk.platforms.server.configuration;

import id.seria.gacha.api.sdk.config.ConfigurationProvider;
import id.seria.gacha.api.sdk.config.YamlConfiguration;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ServerConfigurationProvider implements ConfigurationProvider {
   private ServerPlugin plugin;

   public ServerConfigurationProvider(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public YamlConfiguration load(File file) {
      return new ServerYamlConfiguration(this.plugin, org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file));
   }

   @Override
   public YamlConfiguration load(InputStream stream) {
      Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
      return new ServerYamlConfiguration(this.plugin, org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(reader));
   }

   @Override
   public YamlConfiguration load(Reader reader) {
      return new ServerYamlConfiguration(this.plugin, org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(reader));
   }
}
