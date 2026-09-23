package id.seria.gacha.sdk.platforms.server.commands;

import id.seria.gacha.api.sdk.utilities.Validate;
import id.seria.gacha.sdk.core.commands.PluginCommandManager;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

public class ServerCommandManager extends PluginCommandManager<ServerCommand> {
   private SimpleCommandMap bukkitCommandMap;
   private Map<String, Command> bukkitKnownCommands;

   public ServerCommandManager(ServerPlugin plugin) {
      super(plugin);
      this.setResolvers(new BukkitCommandResolvers(this));
   }

   @Override
   public void init() throws Exception {
      SimplePluginManager pluginManager = (SimplePluginManager)Bukkit.getServer().getPluginManager();
      Field f = SimplePluginManager.class.getDeclaredField("commandMap");
      f.setAccessible(true);
      this.bukkitCommandMap = (SimpleCommandMap)f.get(pluginManager);
      this.bukkitKnownCommands = new HashMap<>();

      try {
         Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
         field.setAccessible(true);
         this.bukkitKnownCommands = (Map<String, Command>)field.get(this.bukkitCommandMap);
      } catch (NoSuchFieldException | IllegalAccessException e) {
         this.plugin.getLogger().error("Failed to access knownCommands via reflection", e);
      }
   }

   @Override
   public void load() throws Exception {
   }

   @Override
   public void unload() throws Exception {
   }

   @Override
   public void reload() throws Exception {
   }

   @Override
   public List<String> getCommandNames() {
      return this.registeredCommands.stream().flatMap(c -> Stream.concat(Stream.of(c.getName()), c.getAliases().stream())).collect(Collectors.toList());
   }

   public boolean registerCommand(final ServerCommand command, boolean enabled) {
      String fallbackPrefix = this.plugin.getName().toLowerCase().trim();
      Command bukkitCommand = new Command(command.getName(), command.getDescription(), "/", command.getAliases()) {
         public boolean execute(CommandSender sender, String label, String[] args) {
            return command.execute(ServerCommandManager.this, sender, label, args);
         }

         public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
            return command.tabComplete(ServerCommandManager.this, sender, alias, args);
         }
      };
      command.register(bukkitCommand);
      synchronized (this.registeredCommands) {
         this.registeredCommands.add(command);
      }

      if (enabled) {
         boolean registered = this.bukkitCommandMap.register(fallbackPrefix, bukkitCommand);
         if (!registered) {
            this.plugin.getLogger().warn("Command \"" + command.getName() + "\" not registered correctly. Perhaps already registered?");
         }
      }

      command.setEnabled(enabled);
      return true;
   }

   public boolean unregisterCommand(ServerCommand command) {
      Validate.notNull(command.getRegisteredCommand(), "Command is not registered");
      Command bukkitCommand = command.getRegisteredCommand();
      if (command.isEnabled()) {
         String fallbackPrefix = this.plugin.getName().toLowerCase().trim();
         String label = bukkitCommand.getLabel().toLowerCase(Locale.ENGLISH).trim();
         boolean removed = this.unregister(label, bukkitCommand, false, fallbackPrefix);

         for (String string : bukkitCommand.getAliases()) {
            this.unregister(string, bukkitCommand, true, fallbackPrefix);
         }

         bukkitCommand.unregister(this.bukkitCommandMap);
      }

      command.unregister(this);
      synchronized (this.registeredCommands) {
         this.registeredCommands.remove(command);
         return true;
      }
   }

   private synchronized boolean unregister(String label, Command command, boolean isAlias, String fallbackPrefix) {
      label = label.toLowerCase(Locale.ENGLISH).trim();
      boolean removed = this.bukkitKnownCommands.remove(fallbackPrefix + ":" + label) != null;
      if (!isAlias && this.bukkitKnownCommands.get(label) == command) {
         this.bukkitKnownCommands.remove(label);
      }

      return removed;
   }

   @Generated
   public SimpleCommandMap getBukkitCommandMap() {
      return this.bukkitCommandMap;
   }

   @Generated
   public Map<String, Command> getBukkitKnownCommands() {
      return this.bukkitKnownCommands;
   }
}
