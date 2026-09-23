package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class GiveCrateCommand extends CrateCommandExecutable {
   public GiveCrateCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      String playerInput = context.getParameterInput("player");
      Player player = context.getComputedParameter("player", Player.class);
      Integer amount = context.getComputedParameter("amount", Integer.class);
      boolean silent = context.getComputedParameter("-s", String.class).equalsIgnoreCase("-s");
      if (player == null) {
         throw new CommandContextException(Translatable.key("player-offline", "%name%", playerInput));
      }

      CrateFacade.giveCrate(crate, player, amount, silent);
      Translations.send(issuer, Translatable.key("crate-given", "%amount%", amount, "%player%", player.getName()));
   }
}
