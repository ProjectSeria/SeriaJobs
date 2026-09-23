package id.seria.gacha.api.sdk.plugin.commands.contexts;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.plugin.Plugin;
import id.seria.gacha.api.sdk.plugin.commands.Command;
import id.seria.gacha.api.sdk.plugin.commands.CommandManager;

public interface InvocationContext {
   CommandManager getManager();

   Command getCommand();

   String getLabel();

   Actor getIssuer();

   default Plugin getPlugin() {
      return this.getManager().getPlugin();
   }
}
