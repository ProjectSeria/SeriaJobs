package id.seria.gacha.api.sdk.plugin.hooks;

import id.seria.gacha.api.sdk.internal.LifecycleObject;

public interface Hook extends LifecycleObject {
   HookRegistry getHookRegistry();

   HookProvider getProvider();

   boolean isEnabled();
}
