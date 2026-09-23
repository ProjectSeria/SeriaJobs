package id.seria.gacha.sdk.platforms.server.commands;

import id.seria.gacha.sdk.core.internal.DefaultCommandResolvers;
import id.seria.gacha.sdk.platforms.server.commands.objects.Players;
import java.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitCommandResolvers extends DefaultCommandResolvers {
   public BukkitCommandResolvers(ServerCommandManager manager) {
      super(manager);
      this.registerResolver(Player.class, (c, stack) -> {
         String nextArg = stack.pop();
         return Bukkit.getPlayer(nextArg);
      });
      this.registerResolver(Players.class, (context, stack) -> {
         String argument = stack.pop();
         switch (argument.toLowerCase()) {
            case "all":
            case "*":
               return new Players(Bukkit.getOnlinePlayers());
            default:
               Player player = Bukkit.getPlayer(argument);
               return player == null ? null : new Players(Collections.singletonList(player));
         }
      });
   }
}
