package id.seria.gacha.api.sdk.plugin.addons;

import java.util.List;

public interface AddonDescription {
   String getName();

   String getDisplayName();

   String getPlugin();

   String getClassLoaderOf();

   List<String> getDepend();

   List<String> getSoftDepend();

   String getVersion();

   String getDescription();

   List<String> getAuthors();
}
