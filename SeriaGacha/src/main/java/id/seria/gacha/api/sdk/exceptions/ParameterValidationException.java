package id.seria.gacha.api.sdk.exceptions;

import id.seria.gacha.api.sdk.locales.translatable.Translation;
import id.seria.gacha.api.sdk.locales.translatable.TranslationProvider;
import lombok.Generated;

public class ParameterValidationException extends RuntimeException {
   private Translation translation;

   public ParameterValidationException(TranslationProvider key, Object... replacements) {
      this(key.getTranslation(), replacements);
   }

   public ParameterValidationException(Translation translation, Object... replacements) {
      super(translation.toString());
      this.translation = translation;
      if (replacements.length > 0) {
         this.translation = translation.arguments(replacements);
      }
   }

   @Generated
   public Translation getTranslation() {
      return this.translation;
   }
}
