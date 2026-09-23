package id.seria.gacha.sdk.core.commands.contexts;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;
import id.seria.gacha.sdk.core.commands.CommandNode;
import id.seria.gacha.sdk.core.commands.PluginCommand;
import id.seria.gacha.sdk.core.commands.PluginCommandManager;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.jetbrains.annotations.Nullable;

public class ParameterInvocationContext implements InvocationContext {
   private final PluginCommandManager manager;
   private final PluginCommand command;
   private String label;
   private final Actor issuer;
   private final CommandNode node;
   private final List<String> arguments;
   private final Map<String, String> parametersInput;
   private final Map<String, Object> computedParameters;

   public ParameterInvocationContext(
      PluginCommandManager manager,
      PluginCommand command,
      String label,
      Actor issuer,
      CommandNode node,
      List<String> arguments,
      Map<String, String> parametersInput,
      Map<String, Object> computedParameters
   ) {
      this.manager = manager;
      this.command = command;
      this.label = label;
      this.node = node;
      this.issuer = issuer;
      this.arguments = arguments;
      this.parametersInput = parametersInput;
      this.computedParameters = computedParameters;
   }

   @Nullable
   public <T> T getComputedParameter(String parameter, Class<T> clazz) {
      if (!this.computedParameters.containsKey(parameter)) {
         throw new IllegalArgumentException("Parameter not found: " + parameter);
      } else {
         Object value = this.computedParameters.get(parameter);
         if (value == null) {
            return null;
         } else if (!clazz.isInstance(value)) {
            throw new ClassCastException("Parameter " + parameter + " is not of type " + clazz.getName());
         } else {
            return clazz.cast(value);
         }
      }
   }

   @Nullable
   public String getParameterInput(String parameter) {
      if (!this.parametersInput.containsKey(parameter)) {
         throw new IllegalArgumentException("Parameter not found: " + parameter);
      } else {
         return this.parametersInput.get(parameter);
      }
   }

   public Map<String, String> getParametersInput() {
      return Collections.unmodifiableMap(this.parametersInput);
   }

   public Map<String, Object> getComputedParameters() {
      return Collections.unmodifiableMap(this.computedParameters);
   }

   @Generated
   public PluginCommandManager getManager() {
      return this.manager;
   }

   @Generated
   public PluginCommand getCommand() {
      return this.command;
   }

   @Generated
   @Override
   public String getLabel() {
      return this.label;
   }

   @Generated
   @Override
   public Actor getIssuer() {
      return this.issuer;
   }

   @Generated
   public CommandNode getNode() {
      return this.node;
   }

   @Generated
   public List<String> getArguments() {
      return this.arguments;
   }
}
