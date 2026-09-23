package id.seria.gacha.sdk.hooks.eco;

import id.seria.gacha.sdk.platforms.server.hooks.AbstractHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class EcoHookProvider extends AbstractHookProvider {
   public EcoHookProvider() {
      super("EcoItems");
      this.setSilent(true);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      EcoProvider hook = new EcoProvider();
      hook.unsafe().setProvider(this);
      return hook;
   }

   @Override
   public boolean isEnabled() {
      try {
         Class.forName("com.willfp.eco.core.items.Items");
         return true;
      } catch (ClassNotFoundException e) {
         return false;
      }
   }

   @Override
   public boolean isAvailable() {
      return this.isEnabled();
   }
}
