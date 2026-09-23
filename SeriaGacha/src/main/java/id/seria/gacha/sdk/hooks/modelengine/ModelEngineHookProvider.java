package id.seria.gacha.sdk.hooks.modelengine;

import id.seria.gacha.sdk.platforms.server.hooks.PluginHookProvider;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import id.seria.gacha.sdk.utilities.api.ProductVersion;

public class ModelEngineHookProvider extends PluginHookProvider {
   public ModelEngineHookProvider() {
      super("ModelEngine");
   }

   @Override
   public boolean isCompatible() {
      String megVersion = this.extractPluginVersion();
      ProductVersion pluginVersion = new ProductVersion(megVersion);
      ProductVersion version400 = new ProductVersion("4.0.0");
      return pluginVersion.compareTo(version400) >= 0;
   }

   @Override
   public ServerHook createHook() {
      ModelEngineHook hook = new ModelEngineHook();
      hook.unsafe().setProvider(this);
      return hook;
   }

   private String extractPluginVersion() {
      String version = this.getPlugin().getDescription().getVersion();
      version = version.replaceAll("[^0-9.]", "");
      if (version.startsWith(".")) {
         version = version.substring(1);
      }

      return version;
   }
}
