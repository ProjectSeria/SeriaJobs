package id.seria.gacha.api.sdk.plugin.extensions;

import id.seria.gacha.api.sdk.plugin.Plugin;
import java.io.IOException;

public interface ExtensionRegistry {
   Plugin getPlugin();

   boolean registerExtension(Extension var1);

   void loadExtension(Extension var1);

   void unloadExtension(Extension var1);

   void unregisterExtension(Extension var1) throws IOException;

   <T extends Extension> T getExtension(Class<T> var1);

   Extension getExtension(String var1);

   boolean isExtensionEnabled(String var1);
}
