package id.seria.gacha.api.sdk.plugin.addons;

import id.seria.gacha.api.sdk.plugin.Plugin;
import java.io.File;
import java.io.IOException;

public interface AddonRegistry {
   Plugin getPlugin();

   boolean initAddon(File var1);

   void loadAddon(Addon var1);

   void unloadAddon(Addon var1);

   void unregisterAddon(Addon var1) throws IOException;

   <T extends Addon> T getAddon(Class<T> var1);

   Addon getAddon(String var1);

   boolean isAddonEnabled(String var1);
}
