package id.seria.gacha.sdk.hooks.craftengine;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class CraftEngineHookProvider extends PluginHookProvider {
   public CraftEngineHookProvider() {
      super("CraftEngine");
      this.setSilent(true);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      CraftEngineProvider hook = new CraftEngineProvider();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
