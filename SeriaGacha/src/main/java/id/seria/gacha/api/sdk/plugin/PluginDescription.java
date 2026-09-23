package id.seria.gacha.api.sdk.plugin;

import java.util.List;

public interface PluginDescription {
   String getName();

   List<String> getDepend();

   List<String> getSoftDepend();

   List<String> getLoadBefore();

   String getVersion();

   String getDescription();

   List<String> getAuthors();

   String getWebsite();
}
