package id.seria.gacha.lib.common.utils.actions.common;

import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;
import org.bukkit.GameMode;

public class GamemodeAction extends Action {
   public GamemodeAction(String[] arguments) {
      super("GAMEMODE", arguments);
   }

   @Override
   public void execute(ActionExecutor executor) throws ActionExecutionException {
      executor.getPlayer().setGameMode(GameMode.valueOf(this.getArgument(0).toUpperCase()));
   }

   @Override
   public String getArgumentsSyntax() {
      return "<gamemode>";
   }
}
