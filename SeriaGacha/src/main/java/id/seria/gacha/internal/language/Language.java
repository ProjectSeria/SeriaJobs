package id.seria.gacha.internal.language;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.Generated;

@Deprecated
public enum Language {
   UNDEFINED("en", "Undefined (Default English)", "Undefined (Default English)"),
   EN_US("en", "English (United States)", "English"),
   AR_SA("ar", "Arabic (Saudi Arabia)", "\u0627\u0644\u0639\u0631\u0628\u064a\u0629"),
   PT_BR("br", "Portuguese (Brazil)", "Portugu\u00eas de Brasil"),
   CS_CZ("cs", "Czech (Czechia)", "\u010ce\u0161tina"),
   DE_DE("de", "German (Germany)", "Deutsch"),
   EL_GR("el", "Greek (Greece)", "\u0395\u03bb\u03bb\u03b7\u03bd\u03b9\u03ba\u03ac"),
   ES_ES("es", "Spanish (Spain)", "Espa\u00f1ol"),
   FR_FR("fr", "French (France)", "Fran\u00e7ais"),
   HI_IN("hi", "Hindi (India)", "\u0939\u093f\u0928\u094d\u0926\u0940"),
   ID_ID("id", "Indonesian (Indonesia)", "Bahasa Indonesia"),
   IT_IT("it", "Italian (Italy)", "Italiano"),
   JA_JP("ja", "Japanese (Japan)", "\u65e5\u672c\u8a9e"),
   KO_KR("ko", "Korean (South Korea)", "\ud55c\uad6d\uc5b4"),
   NL_NL("nl", "Dutch (Netherlands)", "Nederlands"),
   PT_PT("pt", "Portuguese (Portugal)", "Portugu\u00eas de Portugal"),
   PL_PL("pl", "Polish (Poland)", "Polski"),
   RU_RU("ru", "Russian (Russia)", "\u0420\u0443\u0441\u0441\u043a\u0438\u0439"),
   SV_SE("sv", "Swedish (Sweden)", "Svenska"),
   TR_TR("tr", "Turkish (Turkey)", "T\u00fcrk\u00e7e"),
   VI_VN("vi", "Vietnamese (Vietnam)", "Ti\u1ebfng Vi\u1ec7t"),
   ZH_CN("zh", "Chinese (Simplified, China)", "\u4e2d\u6587 (\u7b80\u4f53)");

   private final String symbol;
   private final String localName;
   private final String displayName;

   public static Language getByCountryCode(String countryCode) {
      String[] parts = countryCode.split("_");
      if (parts.length > 0) {
         String countryPart = parts[parts.length - 1];

         for (Language language : values()) {
            if (language.name().equalsIgnoreCase(countryPart) || language.symbol.equalsIgnoreCase(countryPart)) {
               return language;
            }
         }
      }

      return UNDEFINED;
   }

   public static CompletableFuture<Language> fetchLanguageByHostAddress() {
      return CompletableFuture.supplyAsync(() -> {
         try {
            String ipAddress = getHostAddress().get();
            URL url = new URL("https://api.country.is/" + ipAddress);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
               System.out.println("HTTP request failed with response code: " + responseCode);
               return UNDEFINED;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
               response.append(inputLine);
            }

            in.close();
            String countryCode = parseCountryCodeFromResponse(response.toString());
            return getByCountryCode(countryCode);
         } catch (IOException | ExecutionException | InterruptedException e) {
            return UNDEFINED;
         }
      });
   }

   public static CompletableFuture<String> getHostAddress() {
      return CompletableFuture.supplyAsync(() -> {
         try {
            URL url = new URL("https://api.ipify.org");
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ip = reader.readLine();
            reader.close();
            return ip;
         } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve public IP address", e);
         }
      });
   }

   private static String parseCountryCodeFromResponse(String jsonResponse) {
      return ((JsonObject)new Gson().fromJson(jsonResponse, JsonObject.class)).get("country").getAsString();
   }

   @Generated
   public String getSymbol() {
      return this.symbol;
   }

   @Generated
   public String getLocalName() {
      return this.localName;
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   Language(final String symbol, final String localName, final String displayName) {
      this.symbol = symbol;
      this.localName = localName;
      this.displayName = displayName;
   }
}
