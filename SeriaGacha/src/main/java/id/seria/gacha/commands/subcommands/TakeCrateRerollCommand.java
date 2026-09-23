package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class TakeCrateRerollCommand extends CrateCommandExecutable {
   public TakeCrateRerollCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      String playerInput = context.getParameterInput("player");
      Integer amount = context.getComputedParameter("amount", Integer.class);
      this.processPlayerData(issuer, playerInput, data -> {
         data.setCrateRerolls(crate.getIdentifier(), Math.max(0, data.getCrateRerolls(crate.getIdentifier()) - amount));
         Translations.send(issuer, "\u00a7aThe reroll(s) were successfully taken.");
      });
   }
}
