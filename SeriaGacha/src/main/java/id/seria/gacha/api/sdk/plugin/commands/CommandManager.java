package id.seria.gacha.api.sdk.plugin.commands;

import id.seria.gacha.api.sdk.internal.LifecycleObject;
import id.seria.gacha.api.sdk.locales.providers.TranslationsProvider;
import id.seria.gacha.api.sdk.plugin.Plugin;
import java.util.Collection;

public interface CommandManager extends LifecycleObject {
   Plugin getPlugin();

   Collection<? extends Command> getRegisteredCommands();

   CommandResolvers getResolvers();

   default TranslationsProvider getTranslationsProvider() {
      return this.getPlugin().getTranslationsProvider();
   }
}
