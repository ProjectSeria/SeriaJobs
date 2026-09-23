package id.seria.gacha.sdk.core.commands;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translation;
import id.seria.gacha.api.sdk.plugin.commands.Node;
import id.seria.gacha.api.sdk.plugin.commands.components.Executable;
import id.seria.gacha.api.sdk.plugin.commands.components.ParameterValidation;
import id.seria.gacha.api.sdk.plugin.commands.components.TabCompletion;
import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;
import id.seria.gacha.api.sdk.plugin.commands.parameters.Parameter;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import id.seria.gacha.sdk.core.commands.contexts.ParameterInvocationContext;
import id.seria.gacha.sdk.core.internal.Filters;
import id.seria.gacha.sdk.utilities.Objects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import org.jetbrains.annotations.Nullable;

public abstract class CommandNode implements Node<NodeInvocationContext> {
   private final String name;
   @Nullable
   private String description;
   @Nullable
   private String permission;
   private List<NodeParameter> parameters = new ArrayList<>();
   @Nullable
   private Executable<NodeInvocationContext> execution;

   public static CommandNode.CommandNodeBuilder builder(String name) {
      return new CommandNode.CommandNodeBuilder(name);
   }

   public CommandNode(String name) {
      this.name = name;
   }

   public int countRequiredParameters() {
      int count = 0;

      for (Parameter parameter : this.parameters) {
         if (!parameter.isOptional()) {
            count++;
         }
      }

      return count;
   }

   public boolean hasPermission(Actor issuer) {
      if (Objects.isEmpty(this.permission)) {
         return true;
      }

      if (issuer.hasPermission(this.permission)) {
         return true;
      }

      String current = this.permission;

      for (int checks = 0; checks < 32; checks++) {
         int idx = current.lastIndexOf(46);
         if (idx <= 0) {
            break;
         }

         current = current.substring(0, idx);
         if (issuer.hasPermission(current)) {
            return true;
         }
      }

      return false;
   }

   public Translation getUsageSyntax(InvocationContext context) {
      String parameters = this.parameters.stream().map(t -> t.getSyntax(context)).collect(Collectors.joining(" "));
      return CommandTranslationKeys.SYNTAX
         .withArguments(
            "%command%",
            "/" + context.getLabel() + " " + this.name,
            "%arguments%",
            parameters,
            "%description%",
            Optional.ofNullable(this.description).orElse("No description specified")
         );
   }

   public void execute(NodeInvocationContext context) {
      if (this.execution != null) {
         this.execution.execute(context);
      }
   }

   public List<String> tabComplete(NodeInvocationContext context) {
      List<String> args = context.getArguments();
      if (args.isEmpty()) {
         return new ArrayList<>();
      }

      int index = args.size() - 1;
      if (index >= 0 && index < this.getParameters().size()) {
         NodeParameter parameter = this.getParameters().get(index);
         TabCompletion<NodeInvocationContext> completion = parameter.getTabCompletion();
         if (completion == null) {
            return new ArrayList<>();
         }

         List<String> result = completion.tabComplete(context);
         String lastArg = args.get(index);
         return Filters.filter(result, lastArg);
      } else {
         return new ArrayList<>();
      }
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Nullable
   @Generated
   @Override
   public String getDescription() {
      return this.description;
   }

   @Nullable
   @Generated
   @Override
   public String getPermission() {
      return this.permission;
   }

   @Generated
   @Override
   public List<NodeParameter> getParameters() {
      return this.parameters;
   }

   @Nullable
   @Generated
   public Executable<NodeInvocationContext> getExecution() {
      return this.execution;
   }

   @Generated
   public void setDescription(@Nullable String description) {
      this.description = description;
   }

   @Generated
   public void setPermission(@Nullable String permission) {
      this.permission = permission;
   }

   @Generated
   public void setParameters(List<NodeParameter> parameters) {
      this.parameters = parameters;
   }

   @Generated
   public void setExecution(@Nullable Executable<NodeInvocationContext> execution) {
      this.execution = execution;
   }

   public static class CommandNodeBuilder {
      @Generated
      private Executable<NodeInvocationContext> execution;
      private String name;
      private String description;
      private String permission;
      private List<NodeParameter> parameters = new ArrayList<>();
      private Executable<NodeInvocationContext> onExecute;

      public CommandNodeBuilder(String name) {
         this.name = name;
      }

      public CommandNode.CommandNodeBuilder description(String description) {
         this.description = description;
         return this;
      }

      public CommandNode.CommandNodeBuilder permission(String permission) {
         this.permission = permission;
         return this;
      }

      public CommandNode.CommandNodeBuilder parameter(String name, Class<?> type) {
         this.parameters.add(NodeParameter.builder(name, type).build());
         return this;
      }

      public CommandNode.CommandNodeBuilder parameter(String name, Class<?> type, boolean optional) {
         this.parameters.add(NodeParameter.builder(name, type).optional(optional).build());
         return this;
      }

      public <T> CommandNode.CommandNodeBuilder parameter(String name, Class<T> type, boolean optional, T defaultValue) {
         this.parameters.add(NodeParameter.builder(name, type).optional(optional).defaultValue(defaultValue).build());
         return this;
      }

      public CommandNode.CommandNodeBuilder parameter(String name, Class<?> type, TabCompletion<NodeInvocationContext> tabCompletion) {
         this.parameters.add(NodeParameter.builder(name, type).onTabComplete(tabCompletion).build());
         return this;
      }

      public <T> CommandNode.CommandNodeBuilder parameter(
         String name, Class<T> type, TabCompletion<NodeInvocationContext> tabCompletion, ParameterValidation<ParameterInvocationContext, T> validation
      ) {
         this.parameters.add(NodeParameter.builder(name, type).onTabComplete(tabCompletion).onValidate(validation).build());
         return this;
      }

      public CommandNode.CommandNodeBuilder parameters(List<NodeParameter> parameters) {
         this.parameters = parameters;
         return this;
      }

      public CommandNode.CommandNodeBuilder parameters(NodeParameter... params) {
         if (this.parameters == null) {
            this.parameters = new ArrayList<>();
         }

         this.parameters.addAll(Arrays.asList(params));
         return this;
      }

      public CommandNode.CommandNodeBuilder parameters(NodeParameter.NodeParameterBuilder... params) {
         if (this.parameters == null) {
            this.parameters = new ArrayList<>();
         }

         this.parameters.addAll(Arrays.stream(params).map(NodeParameter.NodeParameterBuilder::build).collect(Collectors.toList()));
         return this;
      }

      public CommandNode.CommandNodeBuilder onExecute(Executable<NodeInvocationContext> onExecute) {
         this.onExecute = onExecute;
         return this;
      }

      public CommandNode build() {
         return new CommandNode(this.name) {
            {
               this.setDescription(CommandNodeBuilder.this.description);
               this.setPermission(CommandNodeBuilder.this.permission);
               this.setParameters(CommandNodeBuilder.this.parameters);
               this.setExecution(CommandNodeBuilder.this.onExecute);
            }
         };
      }

      @Generated
      public CommandNode.CommandNodeBuilder name(String name) {
         this.name = name;
         return this;
      }

      @Generated
      public CommandNode.CommandNodeBuilder execution(@Nullable Executable<NodeInvocationContext> execution) {
         this.execution = execution;
         return this;
      }

      @Generated
      @Override
      public String toString() {
         return "CommandNode.CommandNodeBuilder(name="
            + this.name
            + ", description="
            + this.description
            + ", permission="
            + this.permission
            + ", parameters="
            + this.parameters
            + ", execution="
            + this.execution
            + ")";
      }
   }
}
