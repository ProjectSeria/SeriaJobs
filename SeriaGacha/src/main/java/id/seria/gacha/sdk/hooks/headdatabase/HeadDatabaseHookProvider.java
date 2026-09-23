package id.seria.gacha.sdk.hooks.headdatabase;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class HeadDatabaseHookProvider extends PluginHookProvider {
   public HeadDatabaseHookProvider() {
      super("HeadDatabase");
      this.setSilent(true);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      HeadDatabaseProvider hook = new HeadDatabaseProvider();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
