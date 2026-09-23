package id.seria.gacha.commands.subcommands;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class ReloadPluginCommand extends CrateCommandExecutable {
   public ReloadPluginCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) throws Exception {
      PhoenixCrates.getLegacyInstance().reload();
      Translations.send(context.getIssuer(), Translations.t("plugin-reloaded"));
   }
}
