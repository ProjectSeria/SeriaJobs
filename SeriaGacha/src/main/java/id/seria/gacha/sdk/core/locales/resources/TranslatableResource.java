package id.seria.gacha.sdk.core.locales.resources;

import id.seria.gacha.api.sdk.config.ConfigurationSection;
import id.seria.gacha.api.sdk.config.YamlConfiguration;
import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.api.sdk.locales.providers.TranslationFetcher;
import id.seria.gacha.api.sdk.plugin.Plugin;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class TranslatableResource implements TranslationFetcher {
   private final Plugin plugin;
   private final Locale locale;
   private final String resourcePath;

   public TranslatableResource(Plugin plugin, Locale locale, String resourcePath) {
      this.plugin = plugin;
      this.locale = locale;
      this.resourcePath = resourcePath;
   }

   @Override
   public NavigableMap<String, String[]> fetchTranslations() throws Exception {
      YamlConfiguration source = null;
      InputStream input = this.plugin.getResource(this.resourcePath);

      try {
         if (input != null) {
            InputStreamReader stream = new InputStreamReader(input, StandardCharsets.UTF_8);

            try {
               source = this.plugin.getConfigurationProvider().load(stream);
            } catch (Throwable var8) {
               try {
                  stream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            stream.close();
         }
      } catch (Throwable var9) {
         if (input != null) {
            try {
               input.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }
         }

         throw var9;
      }

      if (input != null) {
         input.close();
      }

      if (source == null) {
         this.plugin.getLogger().warn("Translation source file \"" + this.resourcePath + "\" missing.");
      }

      return this.extractRawTranslations(source);
   }

   public NavigableMap<String, String[]> extractRawTranslations(YamlConfiguration configuration) {
      NavigableMap<String, String[]> translations = new TreeMap<>();
      if (configuration == null) {
         return translations;
      }

      ConfigurationSection section = configuration.getSection(this.locale.getPrefix());
      if (section == null) {
         return translations;
      }

      for (String path : section.getKeys(true)) {
         if (!translations.isEmpty()) {
            Entry<String, String[]> entry = translations.lastEntry();
            if (path.contains(entry.getKey())) {
               translations.remove(entry.getKey());
            }
         }

         if (!configuration.isSection(this.locale.getPrefix() + "." + path)) {
            translations.put(path, this.getInConfig(configuration, this.locale.getPrefix() + "." + path));
         }
      }

      return translations;
   }

   private String[] getInConfig(YamlConfiguration configuration, String path) {
      List<String> t = configuration.getStringList(path);
      String[] out;
      if (t.isEmpty()) {
         out = new String[]{configuration.getString(path)};
      } else {
         out = t.toArray(new String[0]);
      }

      return out;
   }
}
