package id.seria.gacha.lib.common.utils.actions.internal;

import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import org.apache.commons.lang.NotImplementedException;

public class DelayAction extends Action {
   public DelayAction(String[] arguments) {
      super("DELAY", arguments);
   }

   @Override
   public void execute(ActionExecutor executor) {
      throw new NotImplementedException();
   }

   @Override
   public String getArgumentsSyntax() {
      return "<delay>";
   }

   public double getDelaySeconds() {
      return Double.valueOf(this.getArgument(0, "0"));
   }
}
