package id.seria.gacha.sdk.core.locales.resources;

import id.seria.gacha.api.sdk.config.ConfigurationSection;
import id.seria.gacha.api.sdk.config.YamlConfiguration;
import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.api.sdk.locales.providers.TranslationFetcher;
import id.seria.gacha.api.sdk.plugin.Plugin;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class TranslatableFile implements TranslationFetcher {
   private final Plugin plugin;
   private final Locale locale;
   private final String sourcePath;
   private final String targetPath;

   public TranslatableFile(Plugin plugin, Locale locale, String path) {
      this(plugin, locale, path, path);
   }

   public TranslatableFile(Plugin plugin, Locale locale, String sourcePath, String targetPath) {
      this.plugin = plugin;
      this.locale = locale;
      this.sourcePath = sourcePath;
      this.targetPath = targetPath;
   }

   @Override
   public NavigableMap<String, String[]> fetchTranslations() throws Exception {
      YamlConfiguration source = null;
      InputStream input = this.plugin.getResource(this.sourcePath);

      try {
         if (input != null) {
            InputStreamReader stream = new InputStreamReader(input, StandardCharsets.UTF_8);

            try {
               source = this.plugin.getConfigurationProvider().load(stream);
            } catch (Throwable var13) {
               try {
                  stream.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }

               throw var13;
            }

            stream.close();
         }
      } catch (Throwable var14) {
         if (input != null) {
            try {
               input.close();
            } catch (Throwable var11) {
               var14.addSuppressed(var11);
            }
         }

         throw var14;
      }

      if (input != null) {
         input.close();
      }

      if (source == null) {
         this.plugin.getLogger().warn("Translation source file \"" + this.sourcePath + "\" missing.");
      }

      File targetFile = new File(this.plugin.getDataFolder(), this.targetPath);
      if (!targetFile.getParentFile().exists()) {
         targetFile.getParentFile().mkdirs();
      }

      if (!targetFile.exists()) {
         if (source == null) {
            return new TreeMap<>();
         }

         source.save(targetFile);
         return this.extractRawTranslations(source);
      } else {
         YamlConfiguration target = this.plugin.getConfigurationProvider().load(targetFile);
         NavigableMap<String, String[]> sourceTranslations = this.extractRawTranslations(source);
         NavigableMap<String, String[]> targetTranslations = this.extractRawTranslations(target);
         NavigableMap<String, String[]> translations = new TreeMap<>();
         boolean yamlUpdated = false;

         for (Entry<String, String[]> entry : sourceTranslations.entrySet()) {
            String[] translation = targetTranslations.get(entry.getKey());
            if (translation != null) {
               translations.put(entry.getKey(), translation);
            } else {
               this.plugin.getLogger().warn("Missing value for key \"" + entry.getKey() + "\". Creating one.");
               if (entry.getValue().length == 1) {
                  target.set(this.locale.getPrefix() + "." + entry.getKey(), entry.getValue()[0]);
               } else {
                  target.set(this.locale.getPrefix() + "." + entry.getKey(), entry.getValue());
               }

               translations.put(entry.getKey(), entry.getValue());
               yamlUpdated = true;
            }
         }

         if (yamlUpdated) {
            target.save(targetFile);
         }

         return translations;
      }
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
