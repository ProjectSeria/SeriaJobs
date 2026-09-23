package id.seria.gacha.sdk.hooks.vault;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class VaultHookProvider extends PluginHookProvider {
   public VaultHookProvider() {
      super("Vault");
      this.setDescription("If you want to use economy in this plugin install it.");
      this.setUrl("https://www.spigotmc.org/resources/vault.34315/");
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      VaultHook hook = new VaultHook();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
