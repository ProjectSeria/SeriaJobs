package id.seria.gacha.lib.common.utils.actions.common;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;
import org.bukkit.entity.Player;

public class TitleAction extends Action {
   private final ActionPlaceholders placeholders;

   public TitleAction(String[] arguments, ActionPlaceholders placeholders) {
      super("TITLE", arguments);
      this.placeholders = placeholders;
   }

   @Override
   public void execute(ActionExecutor executor) throws ActionExecutionException {
      int fadeIn;
      int stay;
      int fadeOut;
      try {
         fadeIn = Integer.parseInt(this.getArgument(2));
         stay = Integer.parseInt(this.getArgument(3));
         fadeOut = Integer.parseInt(this.getArgument(4));
      } catch (NumberFormatException ignored) {
         fadeIn = 1;
         stay = 3;
         fadeOut = 1;
      }

      Utilities.getNMSFactory()
         .sendTitlePacket(
            Lists.newArrayList(new Player[]{executor.getPlayer()}),
            this.placeholders.apply(this.getArgument(0), executor.getPlayer()),
            this.placeholders.apply(this.getArgument(1), executor.getPlayer()),
            fadeIn,
            stay,
            fadeOut
         );
   }

   @Override
   public String getArgumentsSyntax() {
      return "<title>; <subtitle>; <fadein>; <stay>; <fadeout>";
   }
}
