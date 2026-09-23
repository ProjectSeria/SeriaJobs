package id.seria.gacha.sdk.core.locales.providers;

import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.api.sdk.locales.providers.TranslationFetcher;
import id.seria.gacha.api.sdk.locales.providers.TranslationsProvider;
import id.seria.gacha.api.sdk.locales.translatable.Translation;
import id.seria.gacha.api.sdk.locales.translatable.TranslationKey;
import id.seria.gacha.api.sdk.locales.translatable.TranslationLiteral;
import id.seria.gacha.api.sdk.plugin.Plugin;
import id.seria.gacha.sdk.core.locales.resources.TranslatableFile;
import id.seria.gacha.sdk.core.locales.resources.TranslatableResource;
import id.seria.gacha.sdk.utilities.exceptions.NotImplementedException;
import id.seria.gacha.sdk.utilities.placeholders.PlaceholderUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import lombok.Generated;

public class PluginTranslationsProvider implements TranslationsProvider {
   private final Plugin plugin;
   private final String resourcePathSyntax;
   private final String targetPathSyntax;
   private Locale currentLocale;
   private final Map<String, Function<Locale, TranslationFetcher>> translationFetchers = new LinkedHashMap<>();
   private final NavigableMap<String, String[]> translations = new TreeMap<>();

   public PluginTranslationsProvider(Plugin plugin) {
      this(plugin, "translations/translations.%s.yml", "translations/translations.%s.yml");
   }

   public PluginTranslationsProvider(Plugin plugin, String resourcePathSyntax, String targetPathSyntax) {
      this.currentLocale = null;
      this.plugin = plugin;
      this.resourcePathSyntax = resourcePathSyntax;
      this.targetPathSyntax = targetPathSyntax;
   }

   public PluginTranslationsProvider(Plugin plugin, Locale locale) throws Exception {
      this(plugin, locale, "translations/translations.%s.yml", "translations/translations.%s.yml");
   }

   public PluginTranslationsProvider(Plugin plugin, Locale locale, String resourcePathSyntax, String targetPathSyntax) throws Exception {
      this(plugin, resourcePathSyntax, targetPathSyntax);
      this.initialize(locale);
   }

   public void initialize(Locale locale) throws Exception {
      this.changeTranslation(locale);
   }

   @Override
   public void changeTranslation(Locale locale) throws Exception {
      this.currentLocale = locale;
      this.translations.clear();
      this.translations.put("literal", new String[]{"Literal not parsed correctly"});
      this.loadDefaultResource("lang/commands.lang.en.yml", Locale.ENGLISH);
      if (!this.isEnglish(locale)) {
         this.loadLocaleLayer(Locale.ENGLISH);
      }

      this.loadLocaleLayer(locale);
   }

   private void loadLocaleLayer(Locale locale) throws Exception {
      for (Function<Locale, TranslationFetcher> fetcher : this.translationFetchers.values()) {
         this.translations.putAll(fetcher.apply(locale).fetchTranslations());
      }

      TranslatableFile translatable = new TranslatableFile(
         this.plugin, locale, String.format(this.resourcePathSyntax, locale.getPrefix()), String.format(this.targetPathSyntax, locale.getPrefix())
      );
      this.translations.putAll(translatable.fetchTranslations());
   }

   public boolean hasTranslation(Locale locale) {
      if (locale == null) {
         return false;
      }

      try {
         InputStream input = this.plugin.getResource(String.format(this.resourcePathSyntax, locale.getPrefix()));

         boolean var3;
         try {
            var3 = input != null;
         } catch (Throwable var6) {
            if (input != null) {
               try {
                  input.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (input != null) {
            input.close();
         }

         return var3;
      } catch (IOException e) {
         return false;
      }
   }

   private boolean isEnglish(Locale locale) {
      return locale == null || Locale.ENGLISH.getPrefix().equalsIgnoreCase(locale.getPrefix());
   }

   @Override
   public void loadDefaultResource(String resource) throws Exception {
      this.loadDefaultResource(resource, this.currentLocale);
   }

   @Override
   public void loadDefaultResource(String resource, Locale locale) throws Exception {
      TranslatableResource translatable = new TranslatableResource(this.plugin, locale, resource);
      this.translations.putAll(translatable.fetchTranslations());
   }

   @Override
   public void loadTranslations(String namespace, Function<Locale, TranslationFetcher> fetcher) throws Exception {
      this.translationFetchers.put(namespace, fetcher);
      Locale locale = this.currentLocale == null ? Locale.ENGLISH : this.currentLocale;
      if (!this.isEnglish(locale)) {
         this.translations.putAll(fetcher.apply(Locale.ENGLISH).fetchTranslations());
      }

      this.translations.putAll(fetcher.apply(locale).fetchTranslations());
   }

   @Override
   public String[] translateOrNull(Translation translation) {
      if (translation instanceof TranslationKey) {
         return this.translateKeyOrNull((TranslationKey)translation);
      } else if (translation instanceof TranslationLiteral) {
         return this.translateLiteral((TranslationLiteral)translation);
      } else {
         throw new NotImplementedException();
      }
   }

   @Override
   public String[] translate(Translation translation) {
      String[] message = this.translateOrNull(translation);
      if (message == null) {
         this.plugin.getLogger().warn("Missing translation for \"" + translation.asString() + "\"");
         return new String[]{"\u00a7cMissing translation"};
      } else {
         return message;
      }
   }

   private String[] translateKeyOrNull(TranslationKey key) {
      if (key.getKey() == null) {
         return new String[]{"\u00a7cInvalid translation key"};
      }

      String[] message = this.translations.get(key.getKey());
      if (message == null) {
         return null;
      }

      Object[] copyArguments = Arrays.copyOf(key.getArguments(), key.getArguments().length);

      for (int i = 0; i < copyArguments.length; i++) {
         if (copyArguments[i] instanceof Translation) {
            copyArguments[i] = this.translate((Translation)copyArguments[i]);
         } else if (copyArguments[i] instanceof List) {
            List<?> list = (List<?>)copyArguments[i];
            Object[] copySubArguments = Arrays.copyOf(list.toArray(), list.size());

            for (int l = 0; l < copySubArguments.length; l++) {
               if (copySubArguments[l] instanceof Translation) {
                  copySubArguments[l] = this.translate((Translation)copySubArguments[l]);
               }
            }

            copyArguments[i] = copySubArguments;
         }
      }

      String[] result = PlaceholderUtil.replacePlaceholdersAndColors(this.plugin, message, copyArguments);
      if (key.getPostProcessor() != null) {
         for (int i = 0; i < result.length; i++) {
            result[i] = key.getPostProcessor().apply(result[i]);
         }
      }

      return result;
   }

   private String[] translateLiteral(TranslationLiteral literal) {
      if (literal.getLiteral() == null) {
         return new String[]{"\u00a7cInvalid translation literal"};
      }

      Object[] copyArguments = Arrays.copyOf(literal.getArguments(), literal.getArguments().length);

      for (int i = 0; i < copyArguments.length; i++) {
         if (copyArguments[i] instanceof Translation) {
            copyArguments[i] = this.translate((Translation)copyArguments[i]);
         }
      }

      String[] result = literal.getLiteral();
      if (literal.getPostProcessor() != null) {
         for (int i = 0; i < result.length; i++) {
            result[i] = literal.getPostProcessor().apply(result[i]);
         }
      }

      return result;
   }

   @Generated
   @Override
   public Plugin getPlugin() {
      return this.plugin;
   }

   @Generated
   @Override
   public Locale getCurrentLocale() {
      return this.currentLocale;
   }

   @Generated
   public Map<String, Function<Locale, TranslationFetcher>> getTranslationFetchers() {
      return this.translationFetchers;
   }

   @Generated
   @Override
   public NavigableMap<String, String[]> getTranslations() {
      return this.translations;
   }
}
