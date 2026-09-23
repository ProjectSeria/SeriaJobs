package id.seria.gacha.sdk.platforms.server;

import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.api.sdk.plugin.Plugin;
import id.seria.gacha.sdk.core.Core;
import id.seria.gacha.sdk.core.locales.providers.PluginTranslationsProvider;
import id.seria.gacha.sdk.platforms.server.commands.ServerCommandManager;
import id.seria.gacha.sdk.platforms.server.components.ServerLogger;
import id.seria.gacha.sdk.platforms.server.components.ServerPluginDescription;
import id.seria.gacha.sdk.platforms.server.components.ServerScheduler;
import id.seria.gacha.sdk.platforms.server.configuration.ServerConfigurationProvider;
import id.seria.gacha.sdk.platforms.server.eventbus.ServerEventBus;
import id.seria.gacha.sdk.platforms.server.registries.ServerAddonRegistry;
import id.seria.gacha.sdk.platforms.server.registries.ServerExtensionRegistry;
import id.seria.gacha.sdk.platforms.server.registries.ServerHookRegistry;
import id.seria.gacha.sdk.utilities.Cache;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.Generated;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class ServerPlugin implements Plugin {
   @Deprecated
   protected final JavaPlugin original;
   protected final ServerLogger logger;
   protected final ServerConfigurationProvider configurationProvider;
   protected final ServerScheduler scheduler;
   protected final ServerAddonRegistry addonRegistry;
   protected final ServerExtensionRegistry extensionRegistry;
   protected final ServerHookRegistry hookRegistry;
   protected final ServerCommandManager commandManager;
   protected PluginTranslationsProvider translationsProvider;
   protected final ServerEventBus eventBus;
   protected final ServerPluginDescription description;

   public ServerPlugin(JavaPlugin plugin) throws Exception {
      this.original = plugin;
      this.logger = new ServerLogger(this);
      this.configurationProvider = new ServerConfigurationProvider(this);
      this.scheduler = new ServerScheduler(this);
      this.addonRegistry = new ServerAddonRegistry(this);
      this.hookRegistry = new ServerHookRegistry(this);
      this.extensionRegistry = new ServerExtensionRegistry(this);
      this.commandManager = new ServerCommandManager(this);
      this.translationsProvider = new PluginTranslationsProvider(this, Locale.ENGLISH);
      this.eventBus = new ServerEventBus(this);
      this.description = new ServerPluginDescription(this);
   }

   public boolean isFoliaSupported() {
      return Cache.fetch("folia-supported", () -> {
         try {
            InputStream in = this.getResource("plugin.yml");

            Boolean var7;
            label48: {
               Boolean var3;
               try {
                  if (in == null) {
                     var7 = false;
                     break label48;
                  }

                  YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
                  var3 = yaml.getBoolean("folia-supported", false);
               } catch (Throwable var5) {
                  if (in != null) {
                     try {
                        in.close();
                     } catch (Throwable var4) {
                        var5.addSuppressed(var4);
                     }
                  }

                  throw var5;
               }

               if (in != null) {
                  in.close();
               }

               return var3;
            }

            if (in != null) {
               in.close();
            }

            return var7;
         } catch (IOException e) {
            this.getLogger().error("Error while fetching folia-supported property from plugin.yml ", e);
            return false;
         }
      });
   }

   @Override
   public String getName() {
      return this.original.getName();
   }

   @Override
   public File getDataFolder() {
      return this.original.getDataFolder();
   }

   @Override
   public InputStream getResource(String resourceName) {
      return this.original.getResource(resourceName);
   }

   @Override
   public final void init() throws Exception {
      this.translationsProvider.initialize(Locale.ENGLISH);
      Core.initialize(this);
      if (this.addonRegistry != null) {
         this.addonRegistry.initializeAddons();
      }

      this.onInit();
      if (this.original.isEnabled()) {
         if (this.commandManager != null) {
            this.commandManager.init();
         }

         if (this.hookRegistry != null) {
            this.hookRegistry.registerHooks();
         }
      }
   }

   @Override
   public final void load() throws Exception {
      this.onLoad();
      if (this.original.isEnabled()) {
         if (this.addonRegistry != null) {
            this.addonRegistry.loadAddons();
         }

         if (this.hookRegistry != null) {
            this.hookRegistry.loadHooks();
         }

         if (this.extensionRegistry != null) {
            this.extensionRegistry.loadExtensions();
         }

         if (this.commandManager != null) {
            this.commandManager.load();
         }
      }
   }

   @Override
   public final void unload() throws Exception {
      if (this.commandManager != null) {
         this.commandManager.unload();
      }

      if (this.addonRegistry != null) {
         this.addonRegistry.disableAddons();
      }

      if (this.hookRegistry != null) {
         this.hookRegistry.unloadHooks();
      }

      if (this.extensionRegistry != null) {
         this.extensionRegistry.unloadExtensions();
      }

      this.onUnload();
   }

   @Override
   public void reload() throws Exception {
      if (this.addonRegistry != null) {
         this.addonRegistry.disableAddons();
      }

      if (this.hookRegistry != null) {
         this.hookRegistry.unloadHooks();
      }

      if (this.extensionRegistry != null) {
         this.extensionRegistry.unloadExtensions();
      }

      if (this.addonRegistry != null) {
         this.addonRegistry.loadAddons();
      }

      if (this.hookRegistry != null) {
         this.hookRegistry.loadHooks();
      }

      if (this.extensionRegistry != null) {
         this.extensionRegistry.loadExtensions();
      }

      if (this.commandManager != null) {
         this.commandManager.reload();
      }

      this.onReload();
   }

   public abstract void onInit() throws Exception;

   public abstract void onLoad() throws Exception;

   public abstract void onUnload() throws Exception;

   public abstract void onReload() throws Exception;

   @Deprecated
   @Override
   public boolean isEnabled() {
      return this.original.isEnabled();
   }

   @Deprecated
   public abstract boolean isDebugEnabled();

   @NotNull
   public ServerCommandManager getCommandManager() {
      return this.commandManager;
   }

   @Deprecated
   @Generated
   public JavaPlugin getOriginal() {
      return this.original;
   }

   @Generated
   public ServerLogger getLogger() {
      return this.logger;
   }

   @Generated
   public ServerConfigurationProvider getConfigurationProvider() {
      return this.configurationProvider;
   }

   @Generated
   public ServerScheduler getScheduler() {
      return this.scheduler;
   }

   @Generated
   public ServerAddonRegistry getAddonRegistry() {
      return this.addonRegistry;
   }

   @Generated
   public ServerExtensionRegistry getExtensionRegistry() {
      return this.extensionRegistry;
   }

   @Generated
   public ServerHookRegistry getHookRegistry() {
      return this.hookRegistry;
   }

   @Generated
   public PluginTranslationsProvider getTranslationsProvider() {
      return this.translationsProvider;
   }

   @Generated
   public ServerEventBus getEventBus() {
      return this.eventBus;
   }

   @Generated
   public ServerPluginDescription getDescription() {
      return this.description;
   }
}
