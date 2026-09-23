package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class ResetPlayerWinLimitCommand extends CrateCommandExecutable {
   public ResetPlayerWinLimitCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      CustomReward reward = context.getComputedParameter("reward", CustomReward.class);
      String playerInput = context.getParameterInput("player");
      this.processPlayerData(
         issuer,
         playerInput,
         data -> {
            data.resetRewardWinAmount(reward.getIdentifier());
            Translations.send(
               issuer, Translations.t("player-win-limit-reset", "%player%", playerInput, "%reward%", reward.getIdentifier(), "%crate%", crate.getIdentifier())
            );
         }
      );
   }
}
