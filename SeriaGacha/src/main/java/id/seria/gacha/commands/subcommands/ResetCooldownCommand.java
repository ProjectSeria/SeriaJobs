package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class ResetCooldownCommand extends CrateCommandExecutable {
   public ResetCooldownCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      String playerInput = context.getParameterInput("player");
      boolean silent = context.getComputedParameter("-s", String.class).equalsIgnoreCase("-s");
      if (crate.getOpenCooldownSeconds() <= 0) {
         Translations.send(issuer, Translations.t("cooldown-reset-no-cooldown"));
      } else {
         this.processPlayerData(issuer, playerInput, data -> {
            long stored = data.getCrateCooldown(crate.getIdentifier());
            long expiry = stored + (crate.getOpenCooldownSeconds() + 1) * 1000L;
            if (expiry <= System.currentTimeMillis()) {
               Translations.send(issuer, Translations.t("cooldown-reset-not-active"));
            } else {
               data.removeCrateCooldown(crate.getIdentifier());
               Translations.send(issuer, Translations.t("cooldown-reset", "%player%", playerInput, "%crate%", crate.getIdentifier()));
               if (!silent) {
                  Player target = context.getComputedParameter("player", Player.class);
                  if (target != null) {
                     Translations.send(target, Translations.t("cooldown-reset-received", "%crate%", crate.getIdentifier()));
                  }
               }
            }
         });
      }
   }
}
