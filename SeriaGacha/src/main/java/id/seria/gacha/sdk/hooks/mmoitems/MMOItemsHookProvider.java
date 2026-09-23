package id.seria.gacha.sdk.hooks.mmoitems;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class MMOItemsHookProvider extends PluginHookProvider {
   public MMOItemsHookProvider() {
      super("MMOItems");
      this.setSilent(true);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      MMOItemsProvider hook = new MMOItemsProvider();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
