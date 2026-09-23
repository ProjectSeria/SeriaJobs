package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class ResetGlobalWinLimitCooldownCommand extends CrateCommandExecutable {
   public ResetGlobalWinLimitCooldownCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      CustomReward reward = context.getComputedParameter("reward", CustomReward.class);
      boolean reset = Crates.getGlobalRewardStatsTracker().resetCooldown(crate.getIdentifier(), reward.getIdentifier());
      if (!reset) {
         Translations.send(issuer, Translations.t("global-win-limit-cooldown-not-tracked"));
      } else {
         Translations.send(issuer, Translations.t("global-win-limit-cooldown-reset", "%reward%", reward.getIdentifier(), "%crate%", crate.getIdentifier()));
      }
   }
}
