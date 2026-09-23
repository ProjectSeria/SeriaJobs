package id.seria.gacha.sdk.core.commands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.exceptions.ParameterParsingException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translation;
import id.seria.gacha.api.sdk.plugin.commands.ArgumentStack;
import id.seria.gacha.api.sdk.plugin.commands.Command;
import id.seria.gacha.api.sdk.plugin.commands.components.Executable;
import id.seria.gacha.api.sdk.plugin.commands.components.ObjectResolver;
import id.seria.gacha.api.sdk.plugin.commands.components.TabCompletion;
import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;
import id.seria.gacha.api.sdk.utilities.Validate;
import id.seria.gacha.sdk.core.commands.contexts.CommandInvocationContext;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import id.seria.gacha.sdk.core.commands.contexts.ParameterInvocationContext;
import id.seria.gacha.sdk.core.internal.Filters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;

public class PluginCommand implements Command, Executable<CommandInvocationContext>, TabCompletion<CommandInvocationContext> {
   private boolean enabled;
   private final String name;
   private String description;
   private List<String> aliases;
   private Map<String, CommandNode> registeredNodes = new HashMap<>();

   public PluginCommand(String name) {
      this(name, new ArrayList<>());
   }

   public PluginCommand(String name, String... aliases) {
      this(name, Arrays.asList(aliases));
   }

   public PluginCommand(String name, List<String> aliases) {
      this.description = "No description provided";
      this.name = name;
      this.aliases = new ArrayList<>(aliases);
   }

   @Override
   public List<CommandNode> getNodes() {
      return Collections.unmodifiableList(new ArrayList<>(this.registeredNodes.values()));
   }

   public void registerNode(CommandNode.CommandNodeBuilder builder) {
      CommandNode node = builder.build();
      this.registeredNodes.put(node.getName().toLowerCase(), node);
   }

   public void registerNode(CommandNode node) {
      this.registeredNodes.put(node.getName(), node);
   }

   public void execute(CommandInvocationContext context) {
      Validate.isTrue(this.enabled, "Command is not enabled");
      List<String> args = context.getArgs();
      String label = context.getLabel();
      Actor issuer = context.getIssuer();
      if (args.isEmpty()) {
         throw new CommandContextException(CommandTranslationKeys.INFO, "%commands%", this.getInfoNodes(context));
      }

      CommandNode node = this.registeredNodes.get(args.get(0).toLowerCase());
      if (node == null) {
         throw new CommandContextException(CommandTranslationKeys.UNKNOWN_COMMAND, "%command%", "/" + context.getLabel());
      }

      if (!node.hasPermission(issuer)) {
         throw new CommandContextException(CommandTranslationKeys.PERMISSION_DENIED);
      }

      if (args.size() - 1 < node.countRequiredParameters()) {
         throw new CommandContextException(CommandTranslationKeys.USAGE, "%syntax%", node.getUsageSyntax(context));
      }

      NodeInvocationContext nodeContext = this.prepareNodeInvocationContext(context, node, false);
      node.execute(nodeContext);
   }

   private List<Translation> getInfoNodes(InvocationContext context) {
      List<Translation> info = new ArrayList<>();

      for (CommandNode node : this.registeredNodes.values()) {
         if (node.hasPermission(context.getIssuer())) {
            info.add(node.getUsageSyntax(context));
         }
      }

      return info;
   }

   public List<String> tabComplete(CommandInvocationContext context) {
      Validate.isTrue(this.enabled, "Command is not enabled");
      List<String> args = context.getArgs();
      if (args.size() == 1) {
         List<String> tabs = new ArrayList<>();

         for (CommandNode node : this.registeredNodes.values()) {
            if (node.hasPermission(context.getIssuer())) {
               tabs.add(node.getName());
            }
         }

         return Filters.filter(tabs, args.get(0));
      } else {
         CommandNode node = this.registeredNodes.get(args.get(0).toLowerCase());
         if (node == null) {
            return new ArrayList<>();
         }

         if (args.size() - 1 > node.getParameters().size()) {
            return new ArrayList<>();
         }

         NodeInvocationContext nodeContext = this.prepareNodeInvocationContext(context, node, true);
         return node.tabComplete(nodeContext);
      }
   }

   private <T> NodeInvocationContext prepareNodeInvocationContext(CommandInvocationContext commandContext, CommandNode node, boolean skipValidation) throws ParameterParsingException {
      PluginCommandManager manager = commandContext.getManager();
      List<String> arguments = new ArrayList<>(commandContext.getArgs().subList(1, commandContext.getArgs().size()));
      Map<String, String> parametersInput = new HashMap<>();
      Map<String, Object> computedParameters = new HashMap<>();
      ParameterInvocationContext parameterContext = new ParameterInvocationContext(
         manager, this, commandContext.getLabel(), commandContext.getIssuer(), node, new ArrayList<>(arguments), parametersInput, computedParameters
      );
      ArgumentStack argumentStack = new ArgumentStack(arguments);
      int index = 0;

      for (NodeParameter<T> parameter : node.getParameters()) {
         ObjectResolver<ParameterInvocationContext, T> resolver = manager.getResolvers().getClassResolver(parameter.getType());
         T mappedResult = null;
         if (resolver == null) {
            throw new IllegalStateException("Resolver not found for class " + parameter.getType().getSimpleName() + ". Did you registered?");
         }

         if (argumentStack.hasNext()) {
            try {
               mappedResult = resolver.resolve(parameterContext, argumentStack);
            } catch (ParameterParsingException | NullPointerException ex) {
               if (!skipValidation) {
                  throw ex;
               }
            }
         }

         if (mappedResult == null && parameter.getDefaultValue() != null) {
            mappedResult = parameter.getDefaultValue();
         }

         String parameterInput = String.join(" ", argumentStack.getPopAudit());
         argumentStack.clearPopAudit();
         parametersInput.put(parameter.getName().toLowerCase(), parameterInput);
         computedParameters.put(parameter.getName().toLowerCase(), mappedResult);
         if (!skipValidation && parameter.getValidation() != null) {
            parameter.getValidation().validate(parameterContext, mappedResult);
         }
      }

      return new NodeInvocationContext(
         commandContext.getManager(), this, commandContext.getLabel(), commandContext.getIssuer(), node, arguments, parametersInput, computedParameters
      );
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   @Override
   public String getDescription() {
      return this.description;
   }

   @Generated
   @Override
   public List<String> getAliases() {
      return this.aliases;
   }

   @Generated
   public Map<String, CommandNode> getRegisteredNodes() {
      return this.registeredNodes;
   }

   @Generated
   public void setAliases(List<String> aliases) {
      this.aliases = aliases;
   }

   @Generated
   public void setRegisteredNodes(Map<String, CommandNode> registeredNodes) {
      this.registeredNodes = registeredNodes;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public void setDescription(String description) {
      this.description = description;
   }
}
