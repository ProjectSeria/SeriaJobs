package id.seria.gacha.lib.common.utils.actions.internal;

import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;

public class EmptyAction extends Action {
   public EmptyAction() {
      super("EMPTY", new String[0]);
   }

   @Override
   public void execute(ActionExecutor executor) {
   }

   @Override
   public String getArgumentsSyntax() {
      return "";
   }
}
