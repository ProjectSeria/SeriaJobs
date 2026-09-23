package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.managers.reward.WrappedReward;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class GiveCrateRewardCommand extends CrateCommandExecutable {
   public GiveCrateRewardCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CustomReward reward = context.getComputedParameter("reward", CustomReward.class);
      String playerInput = context.getParameterInput("player");
      Player player = context.getComputedParameter("player", Player.class);
      if (player == null) {
         throw new CommandContextException(Translatable.key("player-offline", "%name%", playerInput));
      }

      if (!reward.isValid()) {
         throw new CommandContextException(Translatable.literal("\u00a7cThis reward is not valid. (Empty display item or content items)"));
      }

      WrappedReward wrapped = RewardFacade.wrap(reward);
      RewardFacade.giveReward(wrapped, player);
      Translations.send(issuer, "\u00a7aThe reward was successfully given to " + player.getName() + ".");
   }
}
