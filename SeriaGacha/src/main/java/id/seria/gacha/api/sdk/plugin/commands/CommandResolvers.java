package id.seria.gacha.api.sdk.plugin.commands;

import id.seria.gacha.api.sdk.plugin.commands.components.ObjectResolver;
import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;
import java.util.Map;

public interface CommandResolvers {
   CommandManager getManager();

   Map<Class<?>, ObjectResolver<? extends InvocationContext, ?>> getResolverMap();

   <T> ObjectResolver<? extends InvocationContext, T> getClassResolver(Class<T> var1);
}
