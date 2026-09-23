package id.seria.gacha.api.sdk.locales.providers;

import java.util.NavigableMap;

public interface TranslationFetcher {
   NavigableMap<String, String[]> fetchTranslations() throws Exception;
}
