package id.seria.gacha.sdk.hooks.minimessage;

import id.seria.gacha.sdk.platforms.server.hooks.AbstractHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import java.lang.reflect.Method;
import org.bukkit.command.CommandSender;

public class MiniMessageHookProvider extends AbstractHookProvider {
   public MiniMessageHookProvider() {
      super("MiniMessage");
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      MiniMessageHook hook = new MiniMessageHook();
      hook.unsafe().setProvider(this);
      return hook;
   }

   @Override
   public boolean isEnabled() {
      try {
         Method method = CommandSender.class.getMethod("sendRichMessage", String.class);
         return true;
      } catch (NoSuchMethodException e) {
         return false;
      }
   }

   @Override
   public boolean isAvailable() {
      return this.isEnabled();
   }
}
