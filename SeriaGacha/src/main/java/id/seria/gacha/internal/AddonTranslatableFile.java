package id.seria.gacha.internal;

import id.seria.gacha.api.sdk.config.YamlConfiguration;
import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.api.sdk.locales.providers.TranslationFetcher;
import id.seria.gacha.api.sdk.plugin.addons.Addon;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class AddonTranslatableFile implements TranslationFetcher {
   private final Addon addon;
   private final Locale locale;
   private final String sourcePath;
   private final String targetPath;

   public AddonTranslatableFile(Addon addon, Locale locale, String path) {
      this(addon, locale, path, path);
   }

   public AddonTranslatableFile(Addon addon, Locale locale, String sourcePath, String targetPath) {
      this.addon = addon;
      this.locale = locale;
      this.sourcePath = sourcePath;
      this.targetPath = targetPath;
   }

   @Override
   public NavigableMap<String, String[]> fetchTranslations() throws Exception {
      YamlConfiguration source = null;

      try (InputStream input = this.addon.getResource(this.sourcePath)) {
         if (input != null) {
            try (InputStreamReader stream = new InputStreamReader(input, StandardCharsets.UTF_8)) {
               source = this.addon.getPlugin().getConfigurationProvider().load(stream);
            }
         }
      }

      if (source == null) {
         this.addon.getPlugin().getLogger().warn("Translation source file \"" + this.sourcePath + "\" missing.");
      }

      File targetFile = new File(this.addon.getPlugin().getDataFolder(), this.targetPath);
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
         YamlConfiguration target = this.addon.getPlugin().getConfigurationProvider().load(targetFile);
         NavigableMap<String, String[]> sourceTranslations = this.extractRawTranslations(source);
         NavigableMap<String, String[]> targetTranslations = this.extractRawTranslations(target);
         NavigableMap<String, String[]> translations = new TreeMap<>();
         boolean yamlUpdated = false;

         for (Entry<String, String[]> entry : sourceTranslations.entrySet()) {
            String[] translation = targetTranslations.get(entry.getKey());
            if (translation != null) {
               translations.put(entry.getKey(), translation);
            } else {
               this.addon.getPlugin().getLogger().warn("Missing value for key \"" + entry.getKey() + "\". Creating one.");
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

      for (String path : configuration.getSection(this.locale.getPrefix()).getKeys(true)) {
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
      return t.isEmpty() ? new String[]{configuration.getString(path)} : t.toArray(new String[0]);
   }
}
