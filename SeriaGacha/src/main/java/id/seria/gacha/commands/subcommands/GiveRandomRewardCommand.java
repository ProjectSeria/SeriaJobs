package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.session.OpeningContext;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.internal.generator.RewardsRandomGenerator;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class GiveRandomRewardCommand extends CrateCommandExecutable {
   public GiveRandomRewardCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CrateType crateType = context.getComputedParameter("crate", CrateType.class);
      String playerInput = context.getParameterInput("player");
      Player player = context.getComputedParameter("player", Player.class);
      boolean silent = context.getComputedParameter("-s", String.class).equalsIgnoreCase("-s");
      if (player == null) {
         throw new CommandContextException(Translatable.key("player-offline", "%name%", playerInput));
      }

      if (crateType.getAvailableRewards().isEmpty()) {
         throw new CommandContextException(Translatable.key("no-available-rewards"));
      }

      Crate crate = Crates.getCratesManager().getAnyCrateByType(crateType);
      if (crate == null) {
         throw new CommandContextException(Translatable.key("crate-no-placed-instances", "%crate%", crateType.getIdentifier()));
      }

      OpeningContext.RandomOpeningContext openingContext = (OpeningContext.RandomOpeningContext)OpeningContext.random(player, false, 1);
      RewardsRandomGenerator generator = new RewardsRandomGenerator(crate, openingContext);
      BaseReward reward = generator.generateReward(null);
      RewardFacade.giveReward(reward, player);
      if (!silent) {
         Translations.send(issuer, Translatable.key("random-reward-given", "%player%", player.getName()));
      }
   }
}
