package id.seria.gacha.api.sdk.plugin.commands.components;

import id.seria.gacha.api.sdk.plugin.commands.ArgumentStack;
import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;

public interface ObjectResolver<C extends InvocationContext, O> {
   O resolve(C var1, ArgumentStack var2);
}
