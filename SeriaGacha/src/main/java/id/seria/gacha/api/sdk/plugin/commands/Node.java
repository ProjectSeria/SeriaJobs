package id.seria.gacha.api.sdk.plugin.commands;

import id.seria.gacha.api.sdk.plugin.commands.components.Executable;
import id.seria.gacha.api.sdk.plugin.commands.components.TabCompletion;
import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;
import id.seria.gacha.api.sdk.plugin.commands.parameters.Parameter;
import java.util.List;

public interface Node<T extends InvocationContext> extends Executable<T>, TabCompletion<T> {
   String getName();

   String getDescription();

   String getPermission();

   List<? extends Parameter> getParameters();
}
