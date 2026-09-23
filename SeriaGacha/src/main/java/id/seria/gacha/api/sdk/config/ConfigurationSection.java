package id.seria.gacha.api.sdk.config;

import java.util.List;
import java.util.Set;

public interface ConfigurationSection {
   ConfigurationSection getSection(String var1);

   boolean isSection(String var1);

   void set(String var1, Object var2);

   Object get(String var1);

   boolean isNull(String var1);

   String getString(String var1);

   Boolean getBoolean(String var1);

   int getInt(String var1);

   double getDouble(String var1);

   List<String> getStringList(String var1);

   boolean contains(String var1);

   Set<String> getKeys(boolean var1);
}
