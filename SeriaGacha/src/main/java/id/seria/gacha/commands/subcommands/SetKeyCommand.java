package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class SetKeyCommand extends CrateCommandExecutable {
   public SetKeyCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      Key key = context.getComputedParameter("key", Key.class);
      if (!key.isVirtual()) {
         throw new CommandContextException(Translatable.literal("\u00a7cThis command is only available for crates with virtual keys."));
      }

      String targetInput = context.getParameterInput("player");
      Player target = context.getComputedParameter("player", Player.class);
      Integer amount = context.getComputedParameter("amount", Integer.class);
      if (target == null) {
         this.processPlayerData(issuer, targetInput, data -> {
            data.setVirtualKeys(key.getIdentifier(), amount);
            Translations.send(issuer, Translations.t("key-set", "%amount%", amount, "%player%", targetInput));
         });
      } else {
         this.processPlayerData(issuer, target, data -> {
            data.setVirtualKeys(key.getIdentifier(), amount);
            Translations.send(target, Translations.t("virtual-key-received", "%display_name%", key.getFormattedDisplayName(target), "%amount%", amount)[0]);
            Translations.send(issuer, Translations.t("key-set", "%amount%", amount, "%player%", target.getName()));
         });
      }
   }
}
