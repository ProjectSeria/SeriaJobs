package id.seria.gacha.sdk.platforms.server.registries;

import id.seria.gacha.api.sdk.plugin.extensions.Extension;
import id.seria.gacha.api.sdk.plugin.extensions.ExtensionRegistry;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

public class ServerExtensionRegistry implements ExtensionRegistry {
   private final ServerPlugin plugin;
   private final Map<Class<? extends Extension>, Extension> registeredExtensions = new ConcurrentHashMap<>();
   private final Map<String, Extension> extensionsLookupMap = new HashMap<>();

   public ServerExtensionRegistry(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   public void registerExtensions(Extension... extensions) {
      for (Extension extension : extensions) {
         this.registerExtension(extension);
      }
   }

   @Override
   public boolean registerExtension(Extension extension) {
      Validate.isTrue(this.getExtension(extension.getClass()) == null, "Extension already registered");
      this.registeredExtensions.put((Class<? extends Extension>)extension.getClass(), extension);
      return true;
   }

   public void loadExtensions() {
      for (Extension extension : this.registeredExtensions.values()) {
         this.loadExtension(extension);
      }
   }

   public void unloadExtensions() {
      for (Extension extension : this.registeredExtensions.values()) {
         this.unloadExtension(extension);
      }
   }

   public void unregisterExtensions() throws IOException {
      for (Extension extension : this.registeredExtensions.values()) {
         this.unregisterExtension(extension);
      }
   }

   @Override
   public void loadExtension(Extension extension) {
      Validate.isTrue(this.getExtension(extension.getClass()) != null, "Extension not registered on this registry");
      if (!extension.isEnabled()) {
         Bukkit.getConsoleSender().sendMessage("Enabling extension " + extension.getName());

         try {
            extension.init();
            extension.load();
            extension.unsafe().setEnabled(true);
         } catch (Exception e) {
            this.plugin.getLogger().error("Error loading extension", e);
            this.unloadExtension(extension);
         }
      }
   }

   @Override
   public void unloadExtension(Extension extension) {
      Validate.isTrue(this.getExtension(extension.getClass()) != null, "Extension not registered on this registry");
      if (extension.isEnabled()) {
         Bukkit.getConsoleSender().sendMessage("Disabling extension " + extension.getName());

         try {
            extension.unload();
         } catch (Exception e) {
            this.plugin.getLogger().error("Error unloading extension", e);
         } finally {
            extension.unsafe().setEnabled(false);
         }
      }
   }

   @Override
   public void unregisterExtension(Extension extension) {
      if (extension.isEnabled()) {
         this.unloadExtension(extension);
      }

      this.registeredExtensions.remove(extension.getClass());
   }

   @Override
   public <T extends Extension> T getExtension(Class<T> extensionClazz) {
      Extension extension = this.registeredExtensions.get(extensionClazz);
      return (T)(extension == null ? null : extension);
   }

   @Override
   public Extension getExtension(String name) {
      return this.extensionsLookupMap.get(name);
   }

   @Override
   public boolean isExtensionEnabled(String name) {
      return this.getExtension(name) != null;
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<Class<? extends Extension>, Extension> getRegisteredExtensions() {
      return this.registeredExtensions;
   }

   @Generated
   public Map<String, Extension> getExtensionsLookupMap() {
      return this.extensionsLookupMap;
   }
}
