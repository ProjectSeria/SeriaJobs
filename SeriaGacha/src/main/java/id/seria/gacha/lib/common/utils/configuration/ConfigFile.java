package id.seria.gacha.lib.common.utils.configuration;

import com.google.common.collect.Maps;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ConfigFile {
   private static final Map<String, ConfigFile> files = Maps.newHashMap();
   private final Plugin plugin;
   private final File file;
   private FileConfiguration config;

   public ConfigFile(ServerPlugin plugin, File pathFolder, String fileName) {
      this(plugin.getOriginal(), pathFolder, fileName);
   }

   public ConfigFile(ServerPlugin plugin, String name) {
      this(plugin.getOriginal(), name);
   }

   public ConfigFile(ServerPlugin plugin, String name, String folder) {
      this(plugin.getOriginal(), name, folder);
   }

   public ConfigFile(Plugin plugin, File pathFolder, String fileName) {
      this.plugin = plugin;
      String path = pathFolder.getAbsolutePath() + ":" + fileName;
      if (files.containsKey(path)) {
         this.file = files.get(path).file;
      } else {
         this.file = new File(pathFolder, fileName);
         files.put(path, this);
      }

      this.reloadConfig();
   }

   public ConfigFile(Plugin plugin, String name) {
      this.plugin = plugin;
      String path = plugin.getName() + ":" + name;
      if (files.containsKey(path)) {
         this.file = files.get(path).file;
      } else {
         this.file = new File(plugin.getDataFolder(), name);
         files.put(path, this);
      }

      this.reloadConfig();
   }

   public ConfigFile(Plugin plugin, String name, String folder) {
      this.plugin = plugin;
      String path = plugin.getName() + ":" + folder + "/" + name;
      if (files.containsKey(path)) {
         this.file = files.get(path).file;
      } else {
         File parent = new File(plugin.getDataFolder() + "/" + folder);
         if (!parent.exists()) {
            parent.mkdirs();
         }

         this.file = new File(parent, name);
         files.put(path, this);
      }

      this.reloadConfig();
   }

   public boolean exists() {
      return this.getFile().exists();
   }

   public boolean delete() {
      return this.getFile().delete();
   }

   public void reloadConfig() {
      this.saveDefaultConfig();
      this.config = YamlConfiguration.loadConfiguration(this.file);
   }

   public void saveConfig() {
      try {
         this.getConfig().save(this.file);
      } catch (IOException var2) {
      }
   }

   public void saveDefault() {
      this.getConfig().options().copyDefaults(true);
      this.saveConfig();
   }

   public void saveDefaultConfig() {
      if (!this.file.exists()) {
         this.plugin.saveResource(this.file.getName(), true);
      }
   }

   public ConfigurationSection getConfigurationSection(String path) {
      return this.config.getConfigurationSection(path);
   }

   public void set(String path, Object obj) {
      this.config.set(path, obj);
   }

   public boolean isSet(String path) {
      return this.config.isSet(path);
   }

   public Object get(String path) {
      return this.config.get(path);
   }

   public boolean isNull(String path) {
      return this.get(path) == null;
   }

   public String getString(String path) {
      return this.config.getString(path);
   }

   public boolean isString(String path) {
      return this.config.isString(path);
   }

   public Boolean getBoolean(String path) {
      return this.config.getBoolean(path);
   }

   public int getInt(String path) {
      return this.config.getInt(path);
   }

   public double getDouble(String path) {
      return this.config.getDouble(path);
   }

   public List<String> getStringList(String path) {
      return this.config.getStringList(path);
   }

   public ItemStack getItemStack(String path) {
      return this.config.getItemStack(path);
   }

   public <T> T readSafe(String path, Class<T> cast) {
      try {
         if (this.isNull(path)) {
            throw new IllegalStateException("Field is missing");
         } else if (Enum.class.isAssignableFrom(cast)) {
            return (T)Enum.valueOf((Class)cast, this.getString(path));
         } else if (cast == String[].class) {
            return (T)this.getStringList(path).toArray(new String[0]);
         } else if (cast == List.class) {
            return (T)this.getStringList(path);
         } else if (cast == ItemStack.class) {
            return (T)this.getItemStack(path);
         } else if (cast == Double.class) {
            return (T)Double.valueOf(this.getDouble(path));
         } else {
            return (T)(cast == Boolean.class ? Boolean.valueOf(this.getBoolean(path)) : this.get(path));
         }
      } catch (Exception e) {
         throw new IllegalStateException("Unable to read property \"" + path + "\": " + e.getMessage());
      }
   }

   @Generated
   public Plugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public File getFile() {
      return this.file;
   }

   @Generated
   public FileConfiguration getConfig() {
      return this.config;
   }
}
