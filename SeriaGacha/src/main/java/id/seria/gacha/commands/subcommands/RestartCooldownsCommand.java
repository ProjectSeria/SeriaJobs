package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class RestartCooldownsCommand extends CrateCommandExecutable {
   public RestartCooldownsCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crate = context.getComputedParameter("crate", CrateType.class);
      String rewardIdentifier = context.getComputedParameter("reward", String.class);
      if (rewardIdentifier.equals("*")) {
         int amount = 0;

         for (CustomReward reward : crate.getRegisteredRewards()) {
            if (reward.getTimeLimitedCooldown() > 0L) {
               this.startCooldown(reward);
               amount++;
            }
         }

         if (amount == 0) {
            Translations.send(issuer, Translations.t("reward-cooldown-none-configured"));
         } else {
            crate.invalidateRewardsCache();
            this.saveCrate(crate);
            Translations.send(issuer, Translations.t("reward-cooldown-started-all", "%amount%", String.valueOf(amount), "%crate%", crate.getIdentifier()));
         }
      } else {
         CustomReward reward = crate.getRewardByIdentifier(rewardIdentifier);
         if (reward == null) {
            Translations.send(issuer, Translations.t("crate-reward-dont-exist", "%identifier%", rewardIdentifier));
         } else if (reward.getTimeLimitedCooldown() <= 0L) {
            Translations.send(issuer, Translations.t("reward-cooldown-not-configured"));
         } else {
            this.startCooldown(reward);
            crate.invalidateRewardsCache();
            this.saveCrate(crate);
            Translations.send(issuer, Translations.t("reward-cooldown-started", "%reward%", reward.getIdentifier(), "%crate%", crate.getIdentifier()));
         }
      }
   }

   private void startCooldown(CustomReward reward) {
      reward.setEnabled(true);
      reward.setTimeLimitedExpiry(System.currentTimeMillis() + reward.getTimeLimitedCooldown() * 1000L);
   }

   private void saveCrate(CrateType crate) {
      try {
         Crates.getCratesManager().saveCrateType(crate);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
