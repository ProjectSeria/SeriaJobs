package id.seria.gacha.sdk.hooks.bettermodel;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class BetterModelHookProvider extends PluginHookProvider {
   public BetterModelHookProvider() {
      super("BetterModel");
      this.setSilent(true);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      BetterModelProvider hook = new BetterModelProvider();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
