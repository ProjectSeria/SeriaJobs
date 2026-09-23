package id.seria.gacha.sdk.hooks.playerpoints;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class PlayerPointsHookProvider extends PluginHookProvider {
   public PlayerPointsHookProvider() {
      super("PlayerPoints");
      this.setDescription("If you want to use points as a currency in this plugin install it.");
      this.setUrl("https://www.spigotmc.org/resources/playerpoints.80745/");
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      PlayerPointsHook hook = new PlayerPointsHook();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
