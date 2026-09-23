package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;

public class CheckKeysCommand extends CrateCommandExecutable {
   public CheckKeysCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      String targetInput = context.getParameterInput("player");
      Player target = context.getComputedParameter("player", Player.class);
      String targetName = target != null ? target.getName() : targetInput;
      this.processPlayerData(issuer, targetInput, data -> {
         Translations.send(issuer, Translations.t("check-keys-header", "%player%", targetName));
         Map<String, Integer> virtualKeys = data.getVirtualKeys();
         List<String> lines = new ArrayList<>();

         for (Entry<String, Integer> entry : virtualKeys.entrySet()) {
            if (entry.getValue() > 0) {
               lines.add(Translations.t("check-keys-entry", "%key%", entry.getKey(), "%amount%", entry.getValue())[0]);
            }
         }

         if (lines.isEmpty()) {
            Translations.send(issuer, Translations.t("check-keys-none"));
         } else {
            Translations.send(issuer, lines.toArray(new String[0]));
         }
      });
   }
}
