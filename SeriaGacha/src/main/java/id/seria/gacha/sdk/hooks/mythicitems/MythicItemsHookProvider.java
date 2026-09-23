package id.seria.gacha.sdk.hooks.mythicitems;

import id.seria.gacha.sdk.platforms.server.hooks.AbstractHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class MythicItemsHookProvider extends AbstractHookProvider {
   public MythicItemsHookProvider() {
      super("MythicItems");
      this.setSilent(true);
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      MythicItemsProvider hook = new MythicItemsProvider();
      hook.unsafe().setProvider(this);
      return hook;
   }

   @Override
   public boolean isEnabled() {
      try {
         Class.forName("io.lumine.mythic.core.items.MythicItem");
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
