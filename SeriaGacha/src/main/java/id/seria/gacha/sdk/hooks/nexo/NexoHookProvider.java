package id.seria.gacha.sdk.hooks.nexo;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class NexoHookProvider extends PluginHookProvider {
   public NexoHookProvider() {
      super("Nexo");
      this.setSilent(true);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      NexoProvider hook = new NexoProvider();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
