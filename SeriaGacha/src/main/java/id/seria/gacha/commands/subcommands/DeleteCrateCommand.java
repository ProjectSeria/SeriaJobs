package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class DeleteCrateCommand extends CrateCommandExecutable {
   public DeleteCrateCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      Crates.getCratesManager().unregisterCrateType(crate, true);
      Translations.send(issuer, Translations.t("crate-type-deleted"));
   }
}
