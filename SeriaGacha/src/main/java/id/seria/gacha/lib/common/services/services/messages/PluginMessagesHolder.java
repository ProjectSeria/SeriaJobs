package id.seria.gacha.lib.common.services.services.messages;

import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.workers.ConfigurationWorker;
import id.seria.gacha.lib.common.utils.utility.PlaceholderUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.Arrays;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import lombok.Generated;

public class PluginMessagesHolder {
   private final ServerPlugin plugin;
   private final ConfigurationWorker configurationWorker;
   private final NavigableMap<String, String[]> cache = new TreeMap<>();

   public PluginMessagesHolder(ServerPlugin plugin, Configuration configuration, String sourceFileName, String targetFileName) {
      this.plugin = plugin;
      this.configurationWorker = new ConfigurationWorker(plugin, configuration, sourceFileName, targetFileName);
      this.configurationWorker.getReader().setFieldParser(data -> {
         if (!(data instanceof String)) {
            return data;
         }

         boolean bracketOpen = false;
         int bracketIndex = 0;
         String original = (String)data;
         StringBuilder builder = new StringBuilder();
         char[] chars = original.toCharArray();

         for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (bracketOpen) {
               if (c == ' ' || c == '{') {
                  builder.append(chars[bracketIndex]).append(original.substring(bracketIndex + 1, i)).append(c);
                  bracketOpen = false;
                  bracketIndex = 0;
               } else if (c == '}') {
                  String between = original.substring(bracketIndex + 1, i);
                  builder.append('%').append(StringUtil.camelCaseToUnder(between)).append('%');
                  bracketOpen = false;
                  bracketIndex = 0;
               }
            } else if (c == '{') {
               bracketOpen = true;
               bracketIndex = i;
            } else {
               builder.append(c);
            }
         }

         return builder.toString();
      });
   }

   public void load() throws Exception {
      this.configurationWorker.validate();

      for (Entry<String, Object> entry : this.configurationWorker.computeYamlPaths().entrySet()) {
         this.cache.put(entry.getKey(), this.parseConfigurationObject(entry.getValue()));
      }
   }

   private String[] parseConfigurationObject(Object object) {
      if (object instanceof Object[]) {
         Object[] array = (Object[])object;
         return Arrays.copyOf(array, array.length, String[].class);
      } else if (object instanceof Collection) {
         Object[] array = ((Collection)object).toArray();
         return Arrays.copyOf(array, array.length, String[].class);
      } else {
         return new String[]{object.toString()};
      }
   }

   public void unload() throws Exception {
      this.cache.clear();
   }

   public String[] getCached(String path, Object... values) {
      String[] out = this.cache.get(path);
      if (out == null) {
         this.plugin.getLogger().warn("Requested cached message for \"" + path + "\" but not found.");
         out = new String[]{"\u00a7cInvalid Message"};
      }

      return PlaceholderUtil.replacePlaceholders(this.plugin, Arrays.copyOf(out, out.length), values);
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public ConfigurationWorker getConfigurationWorker() {
      return this.configurationWorker;
   }

   @Generated
   public NavigableMap<String, String[]> getCache() {
      return this.cache;
   }
}
