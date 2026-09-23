package id.seria.gacha.lib.common.utils.actions;

import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import lombok.Generated;
import org.bukkit.entity.Player;

public class ActionExecutor {
   private final Action action;
   private final ServerPlugin plugin;
   private final Player player;

   @Generated
   public Action getAction() {
      return this.action;
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public ActionExecutor(Action action, ServerPlugin plugin, Player player) {
      this.action = action;
      this.plugin = plugin;
      this.player = player;
   }
}
