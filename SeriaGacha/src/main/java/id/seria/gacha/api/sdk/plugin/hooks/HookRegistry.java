package id.seria.gacha.api.sdk.plugin.hooks;

import id.seria.gacha.api.sdk.plugin.Plugin;
import java.util.Set;

public interface HookRegistry {
   Plugin getPlugin();

   Set<? extends HookProvider> getProviders();

   <P extends HookProvider> P getProvider(Class<P> var1);
}
