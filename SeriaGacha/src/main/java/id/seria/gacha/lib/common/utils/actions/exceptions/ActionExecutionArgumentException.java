package id.seria.gacha.lib.common.utils.actions.exceptions;

public class ActionExecutionArgumentException extends ActionExecutionException {
   public ActionExecutionArgumentException() {
      super("Argument missing");
   }
}
