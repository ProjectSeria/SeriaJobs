package id.seria.gacha.api.sdk.plugin.commands.contexts;

import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.plugin.commands.Command;
import id.seria.gacha.api.sdk.plugin.commands.CommandManager;
import id.seria.gacha.api.sdk.plugin.commands.Node;
import java.util.List;

public interface ResolveContext extends FlagHandler {
   CommandManager getManager();

   Command getCommand();

   Node getNode();

   Actor getIssuer();

   List<String> getArguments();

   String popNextArg();

   boolean hasNextArg();
}
