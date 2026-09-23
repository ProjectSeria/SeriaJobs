package id.seria.gacha.lib.common.utils.actions;

import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionArgumentException;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;
import java.util.Arrays;
import lombok.Generated;

public abstract class Action {
   protected final String name;
   private final String[] arguments;

   public abstract void execute(ActionExecutor var1) throws ActionExecutionException;

   public String serialize() {
      return "[" + this.name + "] " + String.join(";", this.arguments);
   }

   public abstract String getArgumentsSyntax();

   public String getArgument(int index) throws ActionExecutionArgumentException {
      if (index >= 0 && index < this.arguments.length) {
         return this.arguments[index];
      } else {
         throw new ActionExecutionArgumentException();
      }
   }

   public String getArgument(int index, String defaultValue) {
      return index >= 0 && index < this.arguments.length ? this.arguments[index] : defaultValue;
   }

   @Generated
   @Override
   public String toString() {
      return "Action(name=" + this.getName() + ", arguments=" + Arrays.deepToString(this.getArguments()) + ")";
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String[] getArguments() {
      return this.arguments;
   }

   @Generated
   public Action(String name, String[] arguments) {
      this.name = name;
      this.arguments = arguments;
   }
}
