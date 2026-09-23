package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class ResetPlayerWinLimitCooldownCommand extends CrateCommandExecutable {
   public ResetPlayerWinLimitCooldownCommand() {
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
            data.setRewardCooldown(reward.getIdentifier(), 0L);
            Translations.send(
               issuer,
               Translations.t("player-win-limit-cooldown-reset", "%player%", playerInput, "%reward%", reward.getIdentifier(), "%crate%", crate.getIdentifier())
            );
         }
      );
   }
}
