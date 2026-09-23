package id.seria.gacha.sdk.hooks.executableitems;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class ExecutableItemsHookProvider extends PluginHookProvider {
   public ExecutableItemsHookProvider() {
      super("ExecutableItems");
      this.setSilent(true);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      ExecutableItemsProvider hook = new ExecutableItemsProvider();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
