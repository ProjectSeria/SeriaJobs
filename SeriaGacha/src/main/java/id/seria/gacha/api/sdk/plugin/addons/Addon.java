package id.seria.gacha.api.sdk.plugin.addons;

import id.seria.gacha.api.sdk.internal.LifecycleObject;
import id.seria.gacha.api.sdk.plugin.Plugin;
import id.seria.gacha.api.sdk.utilities.Validate;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import lombok.Generated;

public abstract class Addon implements LifecycleObject {
   private final Addon.Unsafe unsafe = new Addon.Unsafe();
   private AddonRegistry addonRegistry;
   private Plugin plugin;
   private File file;
   private AddonDescription description;
   private ClassLoader classLoader;
   private boolean enabled;

   public Addon() {
   }

   public Addon.Unsafe unsafe() {
      return this.unsafe;
   }

   public abstract String getRequiredPluginVersion();

   public File getDataFolder() {
      Validate.notNull(this.plugin);
      Validate.notNull(this.description);
      File dataFolder = new File(this.plugin.getDataFolder(), "addons");
      if (!dataFolder.exists()) {
         dataFolder.mkdirs();
      }

      return new File(dataFolder, this.description.getName());
   }

   public String getName() {
      Validate.notNull(this.description);
      return this.description.getName();
   }

   public InputStream getResource(String filename) {
      if (filename == null) {
         throw new IllegalArgumentException("Filename cannot be null");
      }

      Validate.notNull(this.classLoader);

      try {
         URL url = this.classLoader.getResource(filename);
         if (url == null) {
            return null;
         }

         URLConnection connection = url.openConnection();
         connection.setUseCaches(false);
         return connection.getInputStream();
      } catch (IOException ex) {
         return null;
      }
   }

   @Generated
   public AddonRegistry getAddonRegistry() {
      return this.addonRegistry;
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
   public AddonDescription getDescription() {
      return this.description;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   public class Unsafe {
      public Unsafe() {
      }

      public void initialize(AddonRegistry addonRegistry, Plugin plugin, File file, AddonDescription description) {
         Addon.this.addonRegistry = addonRegistry;
         Addon.this.plugin = plugin;
         Addon.this.file = file;
         Addon.this.description = description;
      }

      public void setEnabled(boolean state) {
         Addon.this.enabled = state;
      }

      public void setClassLoader(ClassLoader classLoader) {
         Addon.this.classLoader = classLoader;
      }
   }
}
