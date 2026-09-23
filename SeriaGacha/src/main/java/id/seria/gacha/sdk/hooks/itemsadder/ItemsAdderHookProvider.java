package id.seria.gacha.sdk.hooks.itemsadder;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class ItemsAdderHookProvider extends PluginHookProvider {
   public ItemsAdderHookProvider() {
      super("ItemsAdder");
      this.setSilent(true);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      ItemsAdderProvider hook = new ItemsAdderProvider();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
