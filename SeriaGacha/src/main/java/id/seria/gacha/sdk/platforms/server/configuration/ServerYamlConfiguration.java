package id.seria.gacha.sdk.platforms.server.configuration;

import com.google.common.collect.Sets;
import id.seria.gacha.api.sdk.config.YamlConfiguration;
import id.seria.gacha.api.sdk.plugin.Plugin;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.bukkit.configuration.Configuration;

public class ServerYamlConfiguration implements YamlConfiguration {
   private final Plugin plugin;
   private String resourceFileName;
   private org.bukkit.configuration.file.YamlConfiguration config;

   public ServerYamlConfiguration(Plugin plugin, org.bukkit.configuration.file.YamlConfiguration config) {
      this.plugin = plugin;
      this.config = config;
   }

   @Override
   public void load(File file) {
      this.config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
   }

   @Override
   public void save(File file) {
      try {
         this.config.save(file);
      } catch (IOException e) {
         this.plugin.getLogger().error("Error saving config", e);
      }
   }

   public ServerConfigurationSection getSection(String path) {
      return Optional.ofNullable(this.config.getConfigurationSection(path)).map(ServerConfigurationSection::new).orElse(null);
   }

   @Override
   public boolean isSection(String path) {
      return this.config.get(path) instanceof Configuration;
   }

   @Override
   public void set(String path, Object obj) {
      this.config.set(path, obj);
   }

   @Override
   public Object get(String path) {
      Object object = this.config.get(path);
      return object instanceof Configuration ? new ServerConfigurationSection((Configuration)object) : object;
   }

   @Override
   public boolean isNull(String path) {
      return this.get(path) == null;
   }

   @Override
   public String getString(String path) {
      return this.config.getString(path);
   }

   @Override
   public Boolean getBoolean(String path) {
      return this.config.getBoolean(path);
   }

   @Override
   public int getInt(String path) {
      return this.config.getInt(path);
   }

   @Override
   public double getDouble(String path) {
      return this.config.getDouble(path);
   }

   @Override
   public List<String> getStringList(String path) {
      return this.config.getStringList(path);
   }

   @Override
   public boolean contains(String path) {
      return this.config.contains(path);
   }

   @Override
   public Set<String> getKeys(boolean deep) {
      return Sets.newHashSet(this.config.getKeys(deep));
   }
}
