package id.seria.gacha.sdk.hooks.excellenteconomy;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class ExcellentEconomyHookProvider extends PluginHookProvider {
   public ExcellentEconomyHookProvider() {
      super("ExcellentEconomy");
      this.setDescription("If you want to use ExcellentEconomy currencies in this plugin install it.");
      this.setUrl("https://www.spigotmc.org/resources/excellenteconomy.118702/");
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      ExcellentEconomyHook hook = new ExcellentEconomyHook();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
