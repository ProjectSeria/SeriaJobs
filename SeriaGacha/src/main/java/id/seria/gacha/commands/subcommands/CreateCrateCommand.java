package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class CreateCrateCommand extends CrateCommandExecutable {
   public CreateCrateCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) throws Exception {
      Actor issuer = context.getIssuer();
      String crateIdentifier = context.getArguments().get(0);
      CrateType type = CrateFacade.buildDefault(crateIdentifier);
      Crates.getCratesManager().registerCrateType(type, true);
      Translations.send(issuer, Translations.t("crate-type-created"));
   }
}
