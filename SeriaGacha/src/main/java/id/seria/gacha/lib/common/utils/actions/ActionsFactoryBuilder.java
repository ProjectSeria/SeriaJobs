package id.seria.gacha.lib.common.utils.actions;

import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionArgumentException;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;

public class ActionsFactoryBuilder {
   private final ActionsFactory factory;

   public ActionsFactoryBuilder(ActionsFactory factory) {
      this.factory = factory;
   }

   public ActionsFactoryBuilder action(String name, ActionsFactoryBuilder.ActionBuilderInterface executor) {
      this.factory.registerAction(name, arguments -> new Action(name, arguments) {
         @Override
         public void execute(ActionExecutor ex) throws ActionExecutionException {
            executor.execute(ex, new ActionsFactoryBuilder.ActionArguments(arguments));
         }

         @Override
         public String getArgumentsSyntax() {
            return "";
         }
      });
      return this;
   }

   public ActionsFactory build() {
      return this.factory;
   }

   public static class ActionArguments {
      private final String[] arguments;

      public ActionArguments(String[] arguments) {
         this.arguments = arguments;
      }

      public String get(int index) throws ActionExecutionArgumentException {
         if (index >= 0 && index < this.arguments.length) {
            return this.arguments[index];
         } else {
            throw new ActionExecutionArgumentException();
         }
      }

      public String get(int index, String defaultValue) {
         return index >= 0 && index < this.arguments.length ? this.arguments[index] : defaultValue;
      }
   }

   public interface ActionBuilderInterface {
      void execute(ActionExecutor var1, ActionsFactoryBuilder.ActionArguments var2) throws ActionExecutionException;
   }
}
