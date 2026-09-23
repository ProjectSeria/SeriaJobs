package id.seria.gacha.lib.common.utils.objects;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public interface CustomCommand extends CommandExecutor, TabCompleter {
   default List<String> filter(Stream<String> stream, String filter) {
      if (filter.length() > 0) {
         stream = stream.filter(name -> name.toLowerCase().startsWith(filter.toLowerCase()))
            .sorted(Comparator.comparing(name -> name.toLowerCase().indexOf(filter.toLowerCase())));
      }

      return stream.collect(Collectors.toList());
   }

   default List<String> filter(Collection<String> inputs, String filter) {
      return this.filter(inputs.stream(), filter);
   }

   default Collection<? extends Player> getVisiblePlayers(CommandSender sender) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         return Bukkit.getOnlinePlayers().stream().filter(p -> player.canSee(p)).collect(Collectors.toList());
      } else {
         return Bukkit.getOnlinePlayers();
      }
   }
}
