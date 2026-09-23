package id.seria.gacha.sdk.core.commands;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.sdk.plugin.commands.components.ParameterValidation;
import id.seria.gacha.api.sdk.plugin.commands.components.TabCompletion;
import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;
import id.seria.gacha.api.sdk.plugin.commands.parameters.Parameter;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import id.seria.gacha.sdk.core.commands.contexts.ParameterInvocationContext;
import lombok.Generated;

public class NodeParameter<T> implements Parameter {
   private final String name;
   private final Class<T> type;
   @Deprecated
   private boolean optional;
   private T defaultValue;
   private TabCompletion<NodeInvocationContext> tabCompletion;
   private ParameterValidation<ParameterInvocationContext, T> validation;

   public static <T> NodeParameter.NodeParameterBuilder<T> builder(String name, Class<T> type) {
      return new NodeParameter.NodeParameterBuilder<>(name, type);
   }

   public NodeParameter(String name, Class<T> type) {
      this.name = name;
      this.type = type;
   }

   public String getSyntax(InvocationContext invocation) {
      return this.optional ? "&3[" + this.getDisplayName(invocation) + "]" : "&9<" + this.getDisplayName(invocation) + ">";
   }

   private String getDisplayName(InvocationContext invocation) {
      String[] translated = invocation.getPlugin().getTranslationsProvider().translateOrNull(Translatable.key("command.parameter." + this.name.toLowerCase()));
      return translated != null ? translated[0] : this.name;
   }

   @Generated
   @Override
   public String toString() {
      return "NodeParameter(name="
         + this.getName()
         + ", type="
         + this.getType()
         + ", optional="
         + this.isOptional()
         + ", defaultValue="
         + this.getDefaultValue()
         + ", tabCompletion="
         + this.getTabCompletion()
         + ", validation="
         + this.getValidation()
         + ")";
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   @Override
   public Class<T> getType() {
      return this.type;
   }

   @Deprecated
   @Generated
   @Override
   public boolean isOptional() {
      return this.optional;
   }

   @Generated
   @Override
   public T getDefaultValue() {
      return this.defaultValue;
   }

   @Generated
   public TabCompletion<NodeInvocationContext> getTabCompletion() {
      return this.tabCompletion;
   }

   @Generated
   public ParameterValidation<ParameterInvocationContext, T> getValidation() {
      return this.validation;
   }

   public static class NodeParameterBuilder<T> {
      private final String name;
      private Class<T> type;
      private boolean optional;
      private T defaultValue;
      private TabCompletion<NodeInvocationContext> tabCompletion;
      private ParameterValidation<ParameterInvocationContext, T> validation;

      public NodeParameterBuilder(String name, Class<T> type) {
         this.name = name;
         this.type = type;
      }

      public NodeParameter.NodeParameterBuilder<T> optional() {
         this.optional = true;
         return this;
      }

      public NodeParameter.NodeParameterBuilder<T> optional(boolean optional) {
         this.optional = optional;
         return this;
      }

      public NodeParameter.NodeParameterBuilder<T> defaultValue(T defaultValue) {
         this.defaultValue = defaultValue;
         return this;
      }

      public NodeParameter.NodeParameterBuilder<T> onTabComplete(TabCompletion<NodeInvocationContext> tabCompletion) {
         this.tabCompletion = tabCompletion;
         return this;
      }

      public NodeParameter.NodeParameterBuilder<T> onValidate(ParameterValidation<ParameterInvocationContext, T> validation) {
         this.validation = validation;
         return this;
      }

      public NodeParameter<T> build() {
         NodeParameter<T> nodeParameter = new NodeParameter<>(this.name, this.type);
         nodeParameter.optional = this.optional;
         nodeParameter.defaultValue = this.defaultValue;
         nodeParameter.tabCompletion = this.tabCompletion;
         nodeParameter.validation = this.validation;
         return nodeParameter;
      }
   }
}
