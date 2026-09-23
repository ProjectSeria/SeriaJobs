package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.registries.InformationRegistry;
import id.seria.gacha.registries.ScopedValue;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GiveCrateAllCommand extends CrateCommandExecutable {
   public GiveCrateAllCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      Integer amount = context.getComputedParameter("amount", Integer.class);
      boolean silent = context.getComputedParameter("-s", String.class).equalsIgnoreCase("-s");
      Player[] targets = InformationRegistry.getValue(
         ScopedValue.of(Addons.ACTION_GIVE_CRATE_ALL, crate, Bukkit.getOnlinePlayers()), Addons.IP_LIMIT, Player[].class
      );
      if (targets == null) {
         targets = Bukkit.getOnlinePlayers().toArray(new Player[0]);
      }

      for (Player player : targets) {
         CrateFacade.giveCrate(crate, player, amount, silent);
      }

      String playersDisplay = Translations.t("labels.everyone")[0].toLowerCase();
      Translations.send(issuer, Translatable.key("crate-given", "%amount%", amount, "%player%", playersDisplay));
   }
}
