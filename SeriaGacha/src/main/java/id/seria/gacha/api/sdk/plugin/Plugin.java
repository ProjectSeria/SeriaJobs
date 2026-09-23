package id.seria.gacha.api.sdk.plugin;

import id.seria.gacha.api.sdk.Platform;
import id.seria.gacha.api.sdk.config.ConfigurationProvider;
import id.seria.gacha.api.sdk.config.PluginConfiguration;
import id.seria.gacha.api.sdk.internal.LifecycleObject;
import id.seria.gacha.api.sdk.locales.providers.TranslationsProvider;
import id.seria.gacha.api.sdk.logger.Logger;
import id.seria.gacha.api.sdk.plugin.addons.AddonRegistry;
import id.seria.gacha.api.sdk.plugin.commands.CommandManager;
import id.seria.gacha.api.sdk.plugin.eventbus.EventBus;
import id.seria.gacha.api.sdk.plugin.extensions.ExtensionRegistry;
import id.seria.gacha.api.sdk.plugin.hooks.HookRegistry;
import id.seria.gacha.api.sdk.scheduler.Scheduler;
import java.io.File;
import java.io.InputStream;
import org.jetbrains.annotations.NotNull;

public interface Plugin extends LifecycleObject {
   Platform getPlatform();

   Logger getLogger();

   Scheduler getScheduler();

   @NotNull
   EventBus getEventBus();

   @NotNull
   CommandManager getCommandManager();

   @NotNull
   AddonRegistry getAddonRegistry();

   @NotNull
   ExtensionRegistry getExtensionRegistry();

   @NotNull
   HookRegistry getHookRegistry();

   @NotNull
   TranslationsProvider getTranslationsProvider();

   ConfigurationProvider getConfigurationProvider();

   String getName();

   PluginDescription getDescription();

   File getDataFolder();

   InputStream getResource(String var1);

   boolean isEnabled();

   PluginConfiguration getConfiguration();
}
