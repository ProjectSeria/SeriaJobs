package id.seria.gacha.sdk.platforms.server.configuration;

import com.google.common.collect.Sets;
import id.seria.gacha.api.sdk.config.ConfigurationSection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.bukkit.configuration.Configuration;

public class ServerConfigurationSection implements ConfigurationSection {
   private org.bukkit.configuration.ConfigurationSection section;

   public ServerConfigurationSection(org.bukkit.configuration.ConfigurationSection section) {
      this.section = section;
   }

   public ServerConfigurationSection getSection(String path) {
      return Optional.ofNullable(this.section.getConfigurationSection(path)).map(ServerConfigurationSection::new).orElse(null);
   }

   @Override
   public boolean isSection(String path) {
      return this.section.get(path) instanceof Configuration;
   }

   @Override
   public void set(String path, Object obj) {
      this.section.set(path, obj);
   }

   @Override
   public Object get(String path) {
      Object object = this.section.get(path);
      return object instanceof Configuration ? new ServerConfigurationSection((Configuration)object) : object;
   }

   @Override
   public boolean isNull(String path) {
      return this.get(path) == null;
   }

   @Override
   public String getString(String path) {
      return this.section.getString(path);
   }

   @Override
   public Boolean getBoolean(String path) {
      return this.section.getBoolean(path);
   }

   @Override
   public int getInt(String path) {
      return this.section.getInt(path);
   }

   @Override
   public double getDouble(String path) {
      return this.section.getDouble(path);
   }

   @Override
   public List<String> getStringList(String path) {
      return this.section.getStringList(path);
   }

   @Override
   public boolean contains(String path) {
      return this.section.contains(path);
   }

   @Override
   public Set<String> getKeys(boolean deep) {
      return Sets.newHashSet(this.section.getKeys(deep));
   }
}
