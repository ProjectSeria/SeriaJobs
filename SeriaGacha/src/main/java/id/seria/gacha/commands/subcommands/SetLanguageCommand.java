package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.internal.language.Language;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;

public class SetLanguageCommand extends CrateCommandExecutable {
   public SetLanguageCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) throws Exception {
      Actor issuer = context.getIssuer();
      String identifier = context.getComputedParameter("language", String.class);
      Language language = SetLanguageCommand.Languages.parse(identifier);
      if (language != null && SetLanguageCommand.Languages.isAvailable(language)) {
         Crates.getConfiguration().setLanguage(language);
         Crates.getPlugin().getConfigurationWorker().queueSave();
         PhoenixCrates.getLegacyInstance().switchLanguage(language);
         Translations.send(issuer, Translations.t("language-changed", "%language%", language.getLocalName()));
      } else {
         Translations.send(issuer, Translations.t("language-not-available", "%language%", identifier));
      }
   }

   public static class Languages {
      public Languages() {
      }

      public static Language parse(String identifier) {
         for (Language language : Language.values()) {
            if (language.name().equalsIgnoreCase(identifier)) {
               return language;
            }
         }

         return null;
      }

      public static boolean isAvailable(Language language) {
         return language != null
            && language != Language.UNDEFINED
            && Translations.getProvider().hasTranslation(new Locale(language.getSymbol(), language.getLocalName()));
      }
   }
}
