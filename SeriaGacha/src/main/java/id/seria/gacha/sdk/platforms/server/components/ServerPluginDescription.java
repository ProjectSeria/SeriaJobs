package id.seria.gacha.sdk.platforms.server.components;

import id.seria.gacha.api.sdk.plugin.PluginDescription;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.List;
import lombok.Generated;
import org.bukkit.plugin.PluginDescriptionFile;

public class ServerPluginDescription implements PluginDescription {
   private final String name;
   private final String mainClass;
   private final List<String> depend;
   private final List<String> softDepend;
   private final List<String> loadBefore;
   private final String version;
   private final String description;
   private final List<String> authors;
   private final String website;

   public ServerPluginDescription(ServerPlugin plugin) {
      PluginDescriptionFile pluginDescriptionFile = plugin.getOriginal().getDescription();
      this.name = pluginDescriptionFile.getName();
      this.mainClass = pluginDescriptionFile.getMain();
      this.depend = pluginDescriptionFile.getDepend();
      this.softDepend = pluginDescriptionFile.getSoftDepend();
      this.loadBefore = pluginDescriptionFile.getLoadBefore();
      this.version = pluginDescriptionFile.getVersion();
      this.description = pluginDescriptionFile.getDescription();
      this.authors = pluginDescriptionFile.getAuthors();
      this.website = pluginDescriptionFile.getWebsite();
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   public String getMainClass() {
      return this.mainClass;
   }

   @Generated
   @Override
   public List<String> getDepend() {
      return this.depend;
   }

   @Generated
   @Override
   public List<String> getSoftDepend() {
      return this.softDepend;
   }

   @Generated
   @Override
   public List<String> getLoadBefore() {
      return this.loadBefore;
   }

   @Generated
   @Override
   public String getVersion() {
      return this.version;
   }

   @Generated
   @Override
   public String getDescription() {
      return this.description;
   }

   @Generated
   @Override
   public List<String> getAuthors() {
      return this.authors;
   }

   @Generated
   @Override
   public String getWebsite() {
      return this.website;
   }
}
