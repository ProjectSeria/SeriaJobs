package id.seria.gacha.lib.common.utils.configuration.workers;

import com.google.common.base.Charsets;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.adapter.AdaptersFactory;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.lib.common.utils.configuration.logger.BasicLogger;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Generated;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigurationWorker extends AdaptersFactory {
   protected final Configuration configuration;
   protected final ServerPlugin plugin;
   protected final String sourceFileName;
   protected final String targetFileName;
   protected final ConfigurationWriter writer;
   protected final ConfigurationReader reader;
   protected final Set<String> lastSavedPaths = new HashSet<>();
   protected final BasicLogger logger = new BasicLogger();
   protected final String configVersion = "1725531769000456";

   public ConfigurationWorker(ServerPlugin plugin, Configuration configuration, String sourceFileName, String targetFileName) {
      this.plugin = plugin;
      this.configuration = configuration;
      this.sourceFileName = sourceFileName;
      this.targetFileName = targetFileName;
      this.writer = new ConfigurationWriter(configuration, this);
      this.reader = new ConfigurationReader(this);
   }

   public ConfigurationWorker(ServerPlugin plugin, Configuration configuration, String fileName) {
      this.plugin = plugin;
      this.configuration = configuration;
      this.sourceFileName = fileName;
      this.targetFileName = fileName;
      this.writer = new ConfigurationWriter(configuration, this);
      this.reader = new ConfigurationReader(this);
   }

   public void setSilent(boolean state) {
      this.logger.setSilent(state);
      this.writer.setSilent(state);
      this.reader.setSilent(state);
   }

   public void save() {
      synchronized (this.lastSavedPaths) {
         try {
            File file = this.getConfigurationFile();
            if (!file.getParentFile().exists()) {
               file.getParentFile().mkdirs();
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            this.writer.writeConfiguration(yaml);
            yaml.save(file);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   @Deprecated
   public void validate() throws Exception {
      this.load();
   }

   public void load() throws Exception {
      this.loadConfigurationFromFile();
      this.configuration.onLoad();
   }

   private void loadConfigurationFromFile() throws Exception {
      this.logger.reset();
      InputStream fileStream = this.sourceFileName == null ? null : this.plugin.getResource(this.sourceFileName);

      label77: {
         try {
            if (this.sourceFileName != null && fileStream == null) {
               this.logger.warningAndFlush("No resource with the name \"" + this.sourceFileName + "\" founded.");
            }

            File file = this.getConfigurationFile();
            if (!file.getParentFile().exists()) {
               file.getParentFile().mkdirs();
            }

            boolean fileExists = file.exists();
            if (fileExists) {
               this.logger.infoAndFlush("Loading file \"" + this.targetFileName + "\".");
               YamlConfiguration targetYaml = new YamlConfiguration();
               targetYaml.load(file);
               boolean outdated = this.isOutdated(targetYaml);
               if (!outdated) {
                  this.logger.infoAndFlush("File \"" + this.targetFileName + "\" is up to date. Importing configurations.");
               } else {
                  this.logger
                     .infoAndFlush("File \"" + this.targetFileName + "\" need to be updated. Creating a backup named \"" + this.targetFileName + ".backup\".");
                  file.renameTo(new File(this.plugin.getDataFolder(), this.targetFileName + ".backup"));
                  this.logger.infoAndFlush(ChatColor.AQUA + "Migrating old configurations...");
               }

               this.reader.injectYamlSection(targetYaml, this.configuration);
               if (!outdated) {
                  break label77;
               }
            } else {
               this.logger.infoAndFlush("File \"" + this.targetFileName + "\" not found. Creating file...");
            }

            YamlConfiguration templateYaml = fileStream == null
               ? new YamlConfiguration()
               : YamlConfiguration.loadConfiguration(new InputStreamReader(fileStream, Charsets.UTF_8));
            this.writer.writeConfiguration(templateYaml);
            if (fileExists) {
               this.logger.infoAndFlush("File \"" + this.targetFileName + "\" updated!");
            }

            templateYaml.save(file);
         } catch (Throwable var7) {
            if (fileStream != null) {
               try {
                  fileStream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (fileStream != null) {
            fileStream.close();
         }

         this.logger.reset();
         return;
      }

      if (fileStream != null) {
         fileStream.close();
      }
   }

   public Map<String, Object> computeYamlPaths() {
      return this.writer.computeYamlPaths();
   }

   private boolean isOutdated(YamlConfiguration yaml) {
      try {
         ConfigurationReader reader = new ConfigurationReader(this);
         reader.setSilent(true);
         Configuration cloned = this.configuration.clone();
         reader.injectYamlSection(yaml, cloned);
         int flushedWarnings = reader.getLogger().countFlushed();
         return flushedWarnings > 0;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public File getConfigurationFile() {
      return new File(this.plugin.getDataFolder(), this.targetFileName);
   }

   @Generated
   public Configuration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public String getSourceFileName() {
      return this.sourceFileName;
   }

   @Generated
   public String getTargetFileName() {
      return this.targetFileName;
   }

   @Generated
   public ConfigurationWriter getWriter() {
      return this.writer;
   }

   @Generated
   public ConfigurationReader getReader() {
      return this.reader;
   }

   @Generated
   public Set<String> getLastSavedPaths() {
      return this.lastSavedPaths;
   }

   @Generated
   public BasicLogger getLogger() {
      return this.logger;
   }

   @Generated
   public String getConfigVersion() {
      return this.configVersion;
   }
}
