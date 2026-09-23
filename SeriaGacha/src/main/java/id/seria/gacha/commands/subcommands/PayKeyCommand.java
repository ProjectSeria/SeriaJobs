package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class PayKeyCommand extends CrateCommandExecutable {
   public PayKeyCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) throws Exception {
      Actor issuer = context.getIssuer();
      if (!issuer.isPlayer()) {
         throw new CommandContextException(Translatable.key("only-ingame-players"));
      }

      Player sender = (Player)issuer.raw();
      Key key = context.getComputedParameter("key", Key.class);
      Player target = context.getComputedParameter("player", Player.class);
      Integer amount = context.getComputedParameter("amount", Integer.class);
      if (!key.isVirtual()) {
         throw new CommandContextException(Translatable.literal("\u00a7cYou can only pay virtual keys."));
      }

      if (target == null) {
         throw new CommandContextException(Translatable.key("player-offline", "%name%", context.getParameterInput("player")));
      }

      if (sender.getUniqueId().equals(target.getUniqueId())) {
         throw new CommandContextException(Translatable.literal("\u00a7cYou cannot pay keys to yourself."));
      }

      if (amount <= 0) {
         throw new CommandContextException(Translatable.literal("\u00a7cAmount must be greater than 0."));
      }

      String keyId = key.getIdentifier();
      String targetName = target.getName();
      this.processPlayerData(
         issuer,
         sender,
         senderData -> {
            int senderKeys = senderData.getVirtualKeys(keyId);
            if (senderKeys < amount) {
               Translations.send(sender, "\u00a7cYou don't have enough keys. You have \u00a7e" + senderKeys + "\u00a7c.");
            } else {
               senderData.setVirtualKeys(keyId, senderKeys - amount);
               this.processPlayerData(issuer, target, targetData -> targetData.setVirtualKeys(keyId, targetData.getVirtualKeys(keyId) + amount));
               Translations.send(
                  sender,
                  "\u00a7aYou paid \u00a7e" + amount + "x \u00a7f" + key.getFormattedDisplayName(sender) + " \u00a7ato \u00a7f" + targetName + "\u00a7a."
               );
               Translations.send(
                  target,
                  "\u00a7aYou received \u00a7e"
                     + amount
                     + "x \u00a7f"
                     + key.getFormattedDisplayName(target)
                     + " \u00a7afrom \u00a7f"
                     + sender.getName()
                     + "\u00a7a."
               );
            }
         }
      );
   }
}
