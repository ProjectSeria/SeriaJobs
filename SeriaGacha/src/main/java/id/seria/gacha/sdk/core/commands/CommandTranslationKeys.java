package id.seria.gacha.sdk.core.commands;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.sdk.locales.translatable.Translation;
import id.seria.gacha.api.sdk.locales.translatable.TranslationProvider;
import java.util.Locale;
import lombok.Generated;

public enum CommandTranslationKeys implements TranslationProvider {
   INFO,
   USAGE,
   SYNTAX,
   PERMISSION_DENIED,
   PERMISSION_DENIED_PARAMETER,
   ERROR_GENERIC_LOGGED,
   UNKNOWN_COMMAND,
   INVALID_SYNTAX,
   ERROR_PERFORMING_COMMAND,
   ERROR_PREFIX,
   MUST_BE_A_NUMBER,
   PLEASE_SPECIFY_AT_LEAST,
   PLEASE_SPECIFY_AT_MOST,
   PLAYER_OFFLINE;

   private final Translation translation = Translatable.key("command." + this.name().replace("_", "-").toLowerCase(Locale.ENGLISH));

   CommandTranslationKeys() {
   }

   public Translation withArguments(Object... arguments) {
      return this.translation.arguments(arguments);
   }

   public static Translation error(Translation key) {
      return ERROR_PREFIX.withArguments("%message%", key);
   }

   @Generated
   @Override
   public Translation getTranslation() {
      return this.translation;
   }
}
