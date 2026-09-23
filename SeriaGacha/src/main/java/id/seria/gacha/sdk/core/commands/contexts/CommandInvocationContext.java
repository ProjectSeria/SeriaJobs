package id.seria.gacha.sdk.core.commands.contexts;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;
import id.seria.gacha.sdk.core.commands.PluginCommand;
import id.seria.gacha.sdk.core.commands.PluginCommandManager;
import java.util.List;
import lombok.Generated;

public class CommandInvocationContext implements InvocationContext {
   private PluginCommandManager manager;
   private PluginCommand command;
   private String label;
   private Actor issuer;
   private List<String> args;

   public CommandInvocationContext(PluginCommandManager manager, PluginCommand command, String label, Actor issuer, List<String> args) {
      this.manager = manager;
      this.command = command;
      this.label = label;
      this.issuer = issuer;
      this.args = args;
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
   public List<String> getArgs() {
      return this.args;
   }

   @Generated
   public void setManager(PluginCommandManager manager) {
      this.manager = manager;
   }

   @Generated
   public void setCommand(PluginCommand command) {
      this.command = command;
   }

   @Generated
   public void setLabel(String label) {
      this.label = label;
   }

   @Generated
   public void setIssuer(Actor issuer) {
      this.issuer = issuer;
   }

   @Generated
   public void setArgs(List<String> args) {
      this.args = args;
   }
}
