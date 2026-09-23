package id.seria.gacha.sdk.platforms.server.commands;

import id.seria.gacha.api.sdk.plugin.commands.components.TabCompletion;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabCompletes {
   public static TabCompletion<NodeInvocationContext> ONLINE_PLAYERS = context -> {
      Object sender = context.getIssuer().raw();
      if (sender instanceof Player) {
         Player player = (Player)sender;
         return Bukkit.getOnlinePlayers().stream().filter(player::canSee).<String>map(Player::getName).collect(Collectors.toList());
      } else {
         return Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).collect(Collectors.toList());
      }
   };
   public static TabCompletion<NodeInvocationContext> BOOLEAN = context -> Arrays.asList("y", "n");

   public TabCompletes() {
   }
}
