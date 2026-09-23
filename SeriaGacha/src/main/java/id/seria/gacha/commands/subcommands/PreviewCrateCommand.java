package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.crate.exceptions.CratePreviewException;
import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class PreviewCrateCommand extends CrateCommandExecutable {
   public PreviewCrateCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      Player target;
      if (context.containsParameterInput("player")) {
         target = context.getComputedParameter("player", Player.class);
         if (target == null) {
            throw new CommandContextException(Translatable.key("player-offline", "%name%", context.getParameterInput("player")));
         }
      } else {
         if (!issuer.isPlayer()) {
            throw new CommandContextException(Translatable.literal("\u00a7cTarget is not specified."));
         }

         target = (Player)issuer.raw();
      }

      try {
         CrateFacade.openPreviewMenu(crate, target);
      } catch (CratePreviewException e) {
         Translations.send(issuer, e.getMessage());
      }
   }
}
