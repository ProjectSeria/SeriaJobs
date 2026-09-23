package id.seria.gacha.commands.validations;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.exceptions.ParameterValidationException;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.sdk.plugin.commands.components.ParameterValidation;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import id.seria.gacha.sdk.core.commands.contexts.ParameterInvocationContext;

public class CrateValidations {
   public static ParameterValidation<ParameterInvocationContext, CrateType> THROW_IF_ABSENT = (context, crate) -> {
      if (crate == null) {
         throw new ParameterValidationException(Translatable.key("crate-type-dont-exist", "%identifier%", context.getParameterInput("crate")));
      }
   };
   public static ParameterValidation<ParameterInvocationContext, CrateType> THROW_IF_PRESENT = (context, crate) -> {
      if (crate != null) {
         throw new ParameterValidationException(Translatable.key("crate-type-already-exist", "%identifier%", context.getParameterInput("crate")));
      }
   };

   public CrateValidations() {
   }

   public static void requirePlayerSender(NodeInvocationContext context) {
      if (!context.getIssuer().isPlayer()) {
         throw new CommandContextException(Translatable.key("only-ingame-players"));
      }
   }
}
