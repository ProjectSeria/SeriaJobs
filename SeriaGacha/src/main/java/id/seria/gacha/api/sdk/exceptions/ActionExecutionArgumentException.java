package id.seria.gacha.api.sdk.exceptions;

public class ActionExecutionArgumentException extends ActionExecutionException {
   public ActionExecutionArgumentException() {
      super("Argument missing");
   }
}
