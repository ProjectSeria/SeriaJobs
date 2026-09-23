package id.seria.gacha.lib.common.utils.actions.common;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;
import org.bukkit.entity.Player;

public class ActionBarAction extends Action {
   private final ActionPlaceholders placeholders;

   public ActionBarAction(String[] arguments, ActionPlaceholders placeholders) {
      super("ACTION_BAR", arguments);
      this.placeholders = placeholders;
   }

   @Override
   public void execute(ActionExecutor executor) throws ActionExecutionException {
      Utilities.getNMSFactory()
         .sendActionBarPacket(Lists.newArrayList(new Player[]{executor.getPlayer()}), this.placeholders.apply(this.getArgument(0), executor.getPlayer()));
   }

   @Override
   public String getArgumentsSyntax() {
      return "<message>";
   }
}
