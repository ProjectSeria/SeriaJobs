package id.seria.gacha.sdk.platforms.server.commands;

import id.seria.gacha.api.sdk.exceptions.ParameterValidationException;
import id.seria.gacha.api.sdk.locales.translatable.Translation;
import id.seria.gacha.api.sdk.plugin.commands.components.ParameterValidation;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class Validations {
   public Validations() {
   }

   public static ParameterValidation<NodeInvocationContext, ?> failIfPresent(Translation otherwise) {
      return (context, object) -> {
         if (object != null) {
            throw new ParameterValidationException(otherwise);
         }
      };
   }

   public static ParameterValidation<NodeInvocationContext, ?> failIfNull(Translation otherwise) {
      return (context, object) -> {
         if (object == null) {
            throw new ParameterValidationException(otherwise);
         }
      };
   }
}
