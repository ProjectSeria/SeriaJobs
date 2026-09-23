package id.seria.gacha.sdk.platforms.server.commands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.exceptions.ParameterParsingException;
import id.seria.gacha.api.sdk.exceptions.ParameterValidationException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.utilities.Validate;
import id.seria.gacha.sdk.core.commands.CommandTranslationKeys;
import id.seria.gacha.sdk.core.commands.PluginCommand;
import id.seria.gacha.sdk.core.commands.contexts.CommandInvocationContext;
import id.seria.gacha.sdk.platforms.server.components.actor.CommandActor;
import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ServerCommand extends PluginCommand {
   private Command registeredCommand;

   public ServerCommand(String name) {
      super(name);
   }

   public ServerCommand(String name, String... aliases) {
      super(name, aliases);
   }

   public void register(Command bukkitCommand) {
      Validate.isTrue(this.registeredCommand == null, "Command is already registered");
      this.registeredCommand = bukkitCommand;
   }

   public void unregister(ServerCommandManager manager) {
      Validate.isTrue(this.registeredCommand == null || manager.getRegisteredCommands().contains(this), "Command is not registered in this manager");
      this.registeredCommand = null;
   }

   public boolean execute(ServerCommandManager manager, CommandSender commandSender, String label, String[] args) {
      Actor issuer = new CommandActor(commandSender);
      CommandInvocationContext context = new CommandInvocationContext(manager, this, label, issuer, Arrays.asList(args));

      try {
         super.execute(context);
      } catch (ParameterParsingException ex) {
         commandSender.sendMessage(manager.getTranslationsProvider().translate(CommandTranslationKeys.error(ex.getTranslation())));
      } catch (ParameterValidationException ex) {
         commandSender.sendMessage(manager.getTranslationsProvider().translate(CommandTranslationKeys.error(ex.getTranslation())));
      } catch (CommandContextException ex) {
         commandSender.sendMessage(manager.getTranslationsProvider().translate(ex.getTranslation()));
      }

      return true;
   }

   public List<String> tabComplete(ServerCommandManager manager, CommandSender commandSender, String label, String[] args) {
      Actor issuer = new CommandActor(commandSender);
      CommandInvocationContext context = new CommandInvocationContext(manager, this, label, issuer, Arrays.asList(args));
      return super.tabComplete(context);
   }

   @Generated
   public Command getRegisteredCommand() {
      return this.registeredCommand;
   }
}
