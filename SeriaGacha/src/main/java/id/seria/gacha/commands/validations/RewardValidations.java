package id.seria.gacha.commands.validations;

import id.seria.gacha.api.sdk.exceptions.ParameterValidationException;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.sdk.plugin.commands.components.ParameterValidation;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.core.commands.contexts.ParameterInvocationContext;

public class RewardValidations {
   public static ParameterValidation<ParameterInvocationContext, CustomReward> THROW_IF_ABSENT = (context, reward) -> {
      if (reward == null) {
         throw new ParameterValidationException(Translatable.key("crate-reward-dont-exist", "%identifier%", context.getParameterInput("reward")));
      }
   };

   public RewardValidations() {
   }
}
