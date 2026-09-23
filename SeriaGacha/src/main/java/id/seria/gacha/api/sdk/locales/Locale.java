package id.seria.gacha.api.sdk.locales;

import lombok.Generated;

public class Locale {
   public static final Locale ENGLISH = new Locale("en", "English");
   public static final Locale SPANISH = new Locale("es", "Spanish");
   public static final Locale FRENCH = new Locale("fr", "French");
   public static final Locale GERMAN = new Locale("de", "German");
   public static final Locale ITALIAN = new Locale("it", "Italian");
   public static final Locale PORTUGUESE = new Locale("pt", "Portuguese");
   public static final Locale DUTCH = new Locale("nl", "Dutch");
   public static final Locale RUSSIAN = new Locale("ru", "Russian");
   public static final Locale CHINESE = new Locale("zh", "Chinese (Simplified)");
   public static final Locale JAPANESE = new Locale("ja", "Japanese");
   public static final Locale KOREAN = new Locale("ko", "Korean");
   public static final Locale ARABIC = new Locale("ar", "Arabic");
   public static final Locale HINDI = new Locale("hi", "Hindi");
   public static final Locale TURKISH = new Locale("tr", "Turkish");
   public static final Locale POLISH = new Locale("pl", "Polish");
   public static final Locale SWEDISH = new Locale("sv", "Swedish");
   public static final Locale NORWEGIAN = new Locale("no", "Norwegian");
   public static final Locale FINNISH = new Locale("fi", "Finnish");
   public static final Locale DANISH = new Locale("da", "Danish");
   public static final Locale GREEK = new Locale("el", "Greek");
   public static final Locale HUNGARIAN = new Locale("hu", "Hungarian");
   public static final Locale THAI = new Locale("th", "Thai");
   public static final Locale HEBREW = new Locale("he", "Hebrew");
   public static final Locale CZECH = new Locale("cs", "Czech");
   public static final Locale ROMANIAN = new Locale("ro", "Romanian");
   public static final Locale UKRAINIAN = new Locale("uk", "Ukrainian");
   public static final Locale VIETNAMESE = new Locale("vi", "Vietnamese");
   public static final Locale INDONESIAN = new Locale("id", "Indonesian");
   public static final Locale MALAY = new Locale("ms", "Malay");
   private String prefix;
   private String name;

   @Generated
   public String getPrefix() {
      return this.prefix;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public Locale(String prefix, String name) {
      this.prefix = prefix;
      this.name = name;
   }
}
