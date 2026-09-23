package id.seria.gacha.commands.validations;

import id.seria.gacha.api.sdk.exceptions.ParameterValidationException;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.sdk.plugin.commands.components.ParameterValidation;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.sdk.core.commands.contexts.ParameterInvocationContext;

public class KeyValidations {
   public static ParameterValidation<ParameterInvocationContext, Key> THROW_IF_ABSENT = (context, key) -> {
      if (key == null) {
         throw new ParameterValidationException(Translatable.key("key-dont-exist", "%identifier%", context.getParameterInput("key")));
      }
   };
   public static ParameterValidation<ParameterInvocationContext, Key> THROW_IF_PRESENT = (context, key) -> {
      if (key != null) {
         throw new ParameterValidationException(Translatable.key("crate-type-already-exist", "%identifier%", context.getParameterInput("key")));
      }
   };

   public KeyValidations() {
   }
}
