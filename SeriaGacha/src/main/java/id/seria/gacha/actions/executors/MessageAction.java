package id.seria.gacha.actions.executors;

import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;

public class MessageAction extends Action {
   public MessageAction(String... arguments) {
      super("MESSAGE", arguments);
   }

   @Override
   public void execute(ActionExecutor executor) throws ActionExecutionException {
      Translations.send(executor.getPlayer(), Placeholders.setPlaceholders(String.join(";", this.getArguments()), executor.getPlayer()));
   }

   @Override
   public String getArgumentsSyntax() {
      return "<message...>";
   }
}
