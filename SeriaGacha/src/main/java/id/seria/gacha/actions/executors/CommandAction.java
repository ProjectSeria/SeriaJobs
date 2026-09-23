package id.seria.gacha.actions.executors;

import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;
import org.bukkit.Bukkit;

public class CommandAction extends Action {
   public CommandAction(String[] arguments) {
      super("COMMAND", arguments);
   }

   @Override
   public void execute(ActionExecutor executor) throws ActionExecutionException {
      String command = this.getArgument(1).startsWith("/") ? this.getArgument(1).substring(1) : this.getArgument(1);
      command = command.replace("%player%", executor.getPlayer().getName());
      switch (this.getArgument(0).toLowerCase()) {
         case "console":
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            break;
         case "player":
            executor.getPlayer().chat("/" + command);
            break;
         default:
            throw new IllegalArgumentException("Unknown command target: " + this.getArgument(0));
      }
   }

   @Override
   public String getArgumentsSyntax() {
      return "<console/player>; <command>";
   }

   public enum CommandType {
      CONSOLE,
      PLAYER;

      CommandType() {
      }
   }
}
