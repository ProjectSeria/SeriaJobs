package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.registries.InformationRegistry;
import id.seria.gacha.registries.ScopedValue;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GiveKeyAllCommand extends CrateCommandExecutable {
   public GiveKeyAllCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      Key key = context.getComputedParameter("key", Key.class);
      Integer amount = context.getComputedParameter("amount", Integer.class);
      boolean silent = context.getComputedParameter("-s", String.class).equalsIgnoreCase("-s");
      Player[] targets = InformationRegistry.getValue(
         ScopedValue.of(Addons.ACTION_GIVE_KEY_ALL, key, Bukkit.getOnlinePlayers()), Addons.IP_LIMIT, Player[].class
      );
      if (targets == null) {
         targets = Bukkit.getOnlinePlayers().toArray(new Player[0]);
      }

      int total = targets.length;
      if (total != 0) {
         AtomicInteger completed = new AtomicInteger();
         AtomicInteger failures = new AtomicInteger();

         for (Player player : targets) {
            Runnable finish = () -> {
               if (completed.incrementAndGet() == total) {
                  int failed = failures.get();
                  if (failed == 0) {
                     String playersDisplay = Translations.t("labels.everyone")[0].toLowerCase();
                     Translations.send(issuer, Translatable.key("key-given", "%amount%", amount, "%player%", playersDisplay));
                  } else {
                     Translations.send(
                        issuer,
                        Translatable.key("key-given-all-partial", "%amount%", amount, "%delivered%", total - failed, "%failed%", failed, "%total%", total)
                     );
                  }
               }
            };
            KeyFacade.giveKey(key, player, amount, silent, finish, e -> {
               Crates.getLogger().error("Failed to give key " + key.getIdentifier() + " to " + player.getName(), e);
               failures.incrementAndGet();
               finish.run();
            });
         }
      }
   }
}
