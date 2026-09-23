package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.registries.InformationRegistry;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class GiveKeyCommand extends CrateCommandExecutable {
   public GiveKeyCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      Key key = context.getComputedParameter("key", Key.class);
      String playerInput = context.getParameterInput("player");
      Player player = context.getComputedParameter("player", Player.class);
      Integer amount = context.getComputedParameter("amount", Integer.class);
      boolean silent = context.getComputedParameter("-s", String.class).equalsIgnoreCase("-s");
      if (player == null) {
         if (!key.isVirtual() && !InformationRegistry.isEnabled(Addons.CLAIM)) {
            throw new CommandContextException(Translatable.key("player-offline", "%name%", playerInput));
         }

         KeyFacade.giveKey(
            key,
            playerInput,
            amount,
            silent,
            () -> Translations.send(issuer, Translations.t("key-given-offline", "%amount%", amount, "%players%", playerInput, "%player%", playerInput)),
            e -> {
               Translations.send(issuer, Translations.t("player-not-found", "%name%", playerInput));
               if (!(e instanceof CommandContextException)) {
                  Crates.getLogger().error("An error occurred while giving key '" + key.getIdentifier() + "' to offline player '" + playerInput + "'", e);
               }
            }
         );
      } else {
         KeyFacade.giveKey(key, player, amount, silent);
         Translations.send(issuer, Translatable.key("key-given", "%amount%", amount, "%player%", player.getName()));
      }
   }
}
