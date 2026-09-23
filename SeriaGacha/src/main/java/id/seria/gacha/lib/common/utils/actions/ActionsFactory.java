package id.seria.gacha.lib.common.utils.actions;

import id.seria.gacha.lib.common.utils.actions.internal.DelayAction;
import id.seria.gacha.lib.common.utils.actions.internal.EmptyAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionsFactory implements Cloneable {
   private final Map<String, ActionType> registeredTypes = new HashMap<>();

   public ActionsFactory(Map<String, ActionType> registeredTypes) {
      this.registerAction("DELAY", arguments -> new DelayAction(arguments));
      this.registeredTypes.putAll(registeredTypes);
   }

   public ActionsFactory() {
      this.registerAction("DELAY", arguments -> new DelayAction(arguments));
   }

   public ActionType registerAction(String name, final ActionsFactory.ActionTypeBuilder builder) {
      ActionType type;
      this.registeredTypes.put(name, type = new ActionType(name) {
         @Override
         public Action createAction(String[] arguments) {
            return builder.createAction(arguments);
         }
      });
      return type;
   }

   public void unregisterAction(String name) {
      this.registeredTypes.remove(name);
   }

   public Action unserialize(String format) {
      int firstBracket = format.indexOf(91);
      int secondBracket = format.indexOf(93);
      if (firstBracket != -1 && secondBracket != -1) {
         String rawArguments = secondBracket == format.indexOf("] ") ? format.substring(secondBracket + 2) : format.substring(secondBracket + 1);
         String rawAction = format.substring(firstBracket + 1, secondBracket).replace(" ", "_").toUpperCase();
         String[] arguments = rawArguments.split(";");

         for (int i = 0; i < arguments.length; i++) {
            arguments[i] = arguments[i].trim();
         }

         ActionType action = this.registeredTypes.get(rawAction.toUpperCase());
         if (action != null) {
            return action.createAction(arguments);
         } else {
            throw new IllegalArgumentException("ActionType not implemented: " + rawAction);
         }
      } else {
         return new EmptyAction();
      }
   }

   public String serialize(Action executor) {
      return "[" + executor.getName() + "] " + String.join(";", executor.getArguments());
   }

   public List<String> getRegisteredTypesName() {
      return Collections.unmodifiableList(new ArrayList<>(this.registeredTypes.keySet()));
   }

   public List<ActionType> getRegisteredTypes() {
      return Collections.unmodifiableList(new ArrayList<>(this.registeredTypes.values()));
   }

   public ActionType getTypeByNameOrNull(String name) {
      return this.registeredTypes.get(name);
   }

   public ActionsFactoryBuilder builder() {
      return new ActionsFactoryBuilder(this.clone());
   }

   public ActionsFactory clone() {
      return new ActionsFactory(this.registeredTypes);
   }

   public interface ActionTypeBuilder {
      Action createAction(String[] var1);
   }
}
