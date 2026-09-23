package id.seria.gacha.sdk.core.commands;

import id.seria.gacha.api.sdk.plugin.Plugin;
import id.seria.gacha.api.sdk.plugin.commands.CommandManager;
import id.seria.gacha.api.sdk.plugin.commands.CommandResolvers;
import id.seria.gacha.sdk.core.internal.DefaultCommandResolvers;
import id.seria.gacha.sdk.utilities.exceptions.NotImplementedException;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public abstract class PluginCommandManager<T extends PluginCommand> implements CommandManager {
   protected final Plugin plugin;
   protected final List<T> registeredCommands = new ArrayList<>();
   protected CommandResolvers resolvers;

   public PluginCommandManager(Plugin plugin) {
      this.plugin = plugin;
      this.resolvers = new DefaultCommandResolvers(this);
   }

   public DefaultCommandResolvers getResolvers() {
      return (DefaultCommandResolvers)this.resolvers;
   }

   public abstract boolean registerCommand(T var1, boolean var2);

   public abstract boolean unregisterCommand(T var1);

   public List<String> getCommandNames() {
      throw new NotImplementedException();
   }

   @Generated
   @Override
   public Plugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public List<T> getRegisteredCommands() {
      return this.registeredCommands;
   }

   @Generated
   public void setResolvers(CommandResolvers resolvers) {
      this.resolvers = resolvers;
   }
}
