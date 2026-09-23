package id.seria.gacha.api.sdk.plugin.commands.components;

import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;

public interface ParameterValidation<T extends InvocationContext, O> {
   void validate(T var1, O var2);

   default ParameterValidation<T, O> and(ParameterValidation<T, O> other) {
      return (context, object) -> {
         this.validate(context, object);
         other.validate(context, object);
      };
   }
}
