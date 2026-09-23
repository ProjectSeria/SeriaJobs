package id.seria.gacha.lib.common.utils.plugin;

import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class PhoenixCommand extends Command implements PluginIdentifiableCommand, TabCompleter {
   protected JavaPlugin plugin;

   public PhoenixCommand(ServerPlugin plugin, String name) {
      this(plugin.getOriginal(), name);
   }

   public PhoenixCommand(ServerPlugin plugin, @NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
      this(plugin.getOriginal(), name, description, aliases);
   }

   public PhoenixCommand(ServerPlugin plugin, @NotNull String name, @NotNull String description, @NotNull String... aliases) {
      this(plugin.getOriginal(), name, description, aliases);
   }

   public PhoenixCommand(JavaPlugin plugin, String name) {
      super(name);
      this.plugin = plugin;
   }

   public PhoenixCommand(JavaPlugin plugin, @NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
      super(name, description, "/" + name, aliases);
      this.plugin = plugin;
   }

   public PhoenixCommand(JavaPlugin plugin, @NotNull String name, @NotNull String description, @NotNull String... aliases) {
      super(name, description, "/" + name, Arrays.asList(aliases));
      this.plugin = plugin;
   }

   public abstract void onCommand(CommandSender var1, Command var2, String var3, String[] var4);

   public List<String> onTabComplete(CommandSender sender, Command commands, String commandLabel, String[] arguments) {
      return new ArrayList<>();
   }

   public boolean execute(CommandSender sender, String commandLabel, String[] arguments) {
      this.onCommand(sender, this, commandLabel, arguments);
      return true;
   }

   public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
      return this.onTabComplete(sender, this, alias, args);
   }

   public Plugin getPlugin() {
      return this.plugin;
   }

   public List<String> filter(Stream<String> stream, String filter) {
      if (filter.length() > 0) {
         stream = stream.filter(name -> name.toLowerCase().startsWith(filter.toLowerCase()))
            .sorted(Comparator.comparing(name -> name.toLowerCase().indexOf(filter.toLowerCase())));
      }

      return stream.collect(Collectors.toList());
   }

   public List<String> filter(Collection<String> inputs, String filter) {
      return this.filter(inputs.stream(), filter);
   }

   public Collection<? extends Player> getVisiblePlayers(CommandSender sender) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         return Bukkit.getOnlinePlayers().stream().filter(p -> player.canSee(p)).collect(Collectors.toList());
      } else {
         return Bukkit.getOnlinePlayers();
      }
   }
}
