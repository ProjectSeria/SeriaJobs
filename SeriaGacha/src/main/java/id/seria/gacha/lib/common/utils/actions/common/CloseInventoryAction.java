package id.seria.gacha.lib.common.utils.actions.common;

import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;

public class CloseInventoryAction extends Action {
   public CloseInventoryAction() {
      super("CLOSE_INVENTORY", new String[0]);
   }

   @Override
   public void execute(ActionExecutor executor) throws ActionExecutionException {
      executor.getPlayer().closeInventory();
      executor.getPlayer().updateInventory();
   }

   @Override
   public String getArgumentsSyntax() {
      return "";
   }
}
