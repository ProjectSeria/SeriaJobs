package id.seria.gacha.api.sdk.plugin.commands.components;

import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;

public interface Executable<T extends InvocationContext> {
   void execute(T var1);
}
