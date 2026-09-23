package id.seria.gacha.api.sdk.locales.providers;

import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.api.sdk.locales.translatable.Translation;
import id.seria.gacha.api.sdk.plugin.Plugin;
import java.util.NavigableMap;
import java.util.function.Function;

public interface TranslationsProvider {
   Plugin getPlugin();

   Locale getCurrentLocale();

   NavigableMap<String, String[]> getTranslations();

   void changeTranslation(Locale var1) throws Exception;

   void loadDefaultResource(String var1) throws Exception;

   void loadDefaultResource(String var1, Locale var2) throws Exception;

   void loadTranslations(String var1, Function<Locale, TranslationFetcher> var2) throws Exception;

   String[] translate(Translation var1);

   String[] translateOrNull(Translation var1);
}
