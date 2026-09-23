package id.seria.gacha.sdk.hooks.votingplugin;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;

public class VotingPluginHookProvider extends PluginHookProvider {
   public VotingPluginHookProvider() {
      super("VotingPlugin");
      this.setDescription("If you want to use vote points as a currency in this plugin install it.");
      this.setUrl("https://www.spigotmc.org/resources/votingplugin.15358/");
   }

   @Override
   public boolean isCompatible() {
      return true;
   }

   @Override
   public ServerHook createHook() {
      VotingPluginHook hook = new VotingPluginHook();
      hook.unsafe().setProvider(this);
      return hook;
   }
}
