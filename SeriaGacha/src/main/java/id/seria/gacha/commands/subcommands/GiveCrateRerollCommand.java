package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class GiveCrateRerollCommand extends CrateCommandExecutable {
   public GiveCrateRerollCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      String playerInput = context.getParameterInput("player");
      Player player = context.getComputedParameter("player", Player.class);
      Integer amount = context.getComputedParameter("amount", Integer.class);
      if (player == null) {
         this.processPlayerData(issuer, playerInput, data -> {
            data.setCrateRerolls(crate.getIdentifier(), data.getCrateRerolls(crate.getIdentifier()) + amount);
            Translations.send(issuer, "\u00a7aThe reroll(s) were successfully given.");
         });
      } else {
         this.giveReRollsToPlayer(issuer, crate, player, amount);
      }
   }

   private void giveReRollsToPlayer(Actor sender, CrateType crate, Player player, int amount) {
      Crates.getPlayersManager().fetchPlayerDataAsync(player).whenComplete((data, ex) -> {
         if (ex != null) {
            Translations.send(sender, "\u00a7cAn error occurred: " + ex.getMessage());
         } else {
            data.setCrateRerolls(crate.getIdentifier(), data.getCrateRerolls(crate.getIdentifier()) + amount);
            Translations.send(player, Translations.t("crate-rerolls-received", "%display_name%", crate.getFormattedDisplayName(player), "%amount%", amount));
            Translations.send(sender, "\u00a7aThe rerolls were successfully given to " + player.getName() + ".");
         }
      });
   }
}
