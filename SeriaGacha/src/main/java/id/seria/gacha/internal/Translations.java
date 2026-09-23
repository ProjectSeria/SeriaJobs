package id.seria.gacha.internal;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.sdk.locales.translatable.Translation;
import id.seria.gacha.internal.language.Language;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.sdk.core.locales.providers.PluginTranslationsProvider;
import id.seria.gacha.sdk.hooks.minimessage.MiniMessageHook;
import id.seria.gacha.sdk.hooks.minimessage.MiniMessageHookProvider;
import id.seria.gacha.sdk.utilities.Cache;
import id.seria.gacha.sdk.utilities.placeholders.PlaceholderUtil;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.command.CommandSender;

public class Translations {
   private static PluginTranslationsProvider PROVIDER;

   public Translations() {
   }

   public static PluginTranslationsProvider getProvider() {
      return Objects.requireNonNull(PROVIDER, "Editor Facade not initialized");
   }

   public static void clear() {
      PROVIDER = null;
   }

   public static void register(PhoenixCrates plugin) throws Exception {
      Validate.isTrue(PROVIDER == null, "Translation Provider already registered", new Object[0]);
      Language language = plugin.getConfiguration().getLanguage();
      PROVIDER = plugin.getTranslationsProvider();
      PROVIDER.changeTranslation(new Locale(language.getSymbol(), language.getLocalName()));
   }

   public static void reload() throws Exception {
      Validate.isTrue(PROVIDER != null, "Messages not registered", new Object[0]);
      Language language = Crates.getConfiguration().getLanguage();
      PROVIDER.changeTranslation(new Locale(language.getSymbol(), language.getLocalName()));
   }

   public static String[] t(Translation translatable) {
      Validate.isTrue(PROVIDER != null, "Translations not registered", new Object[0]);
      return PROVIDER.translate(translatable);
   }

   public static String[] t(String path, Object... values) {
      Validate.isTrue(PROVIDER != null, "Translations not registered", new Object[0]);
      return PROVIDER.translate(Translatable.key(path, values));
   }

   public static List<String> r(@NonNull List<String> rawMessage, Object... values) {
      if (rawMessage == null) {
         throw new NullPointerException("rawMessage is marked non-null but is null");
      }

      Validate.isTrue(PROVIDER != null, "Messages not registered", new Object[0]);
      String[] message = PlaceholderUtil.replacePlaceholders(PROVIDER.getPlugin(), rawMessage.toArray(new String[0]), values);
      return Lists.newArrayList(Utilities.getNMSFactory().translateColorCodes(message));
   }

   public static String[] r(String[] rawMessage, Object... values) {
      Validate.isTrue(PROVIDER != null, "Messages not registered", new Object[0]);
      String[] message = PlaceholderUtil.replacePlaceholders(PROVIDER.getPlugin(), rawMessage, values);
      return Utilities.getNMSFactory().translateColorCodes(message);
   }

   public static String r(String rawMessage, Object... values) {
      Validate.isTrue(PROVIDER != null, "Messages not registered", new Object[0]);
      return PlaceholderUtil.replacePlaceholdersAndColors(PROVIDER.getPlugin(), new String[]{rawMessage}, values)[0];
   }

   public static void send(CommandSender actor, String... message) {
      if (message.length != 1 || !message[0].isEmpty()) {
         _send(actor, message);
      }
   }

   public static void send(Actor actor, String... message) {
      if (message.length != 1 || !message[0].isEmpty()) {
         Validate.isTrue(actor.raw() instanceof CommandSender, "Actor not command sender", new Object[0]);
         _send((CommandSender)actor.raw(), message);
      }
   }

   public static void send(CommandSender actor, Translatable translation) {
      String[] message = PROVIDER.translate(translation);
      if (message.length != 1 || !message[0].isEmpty()) {
         _send(actor, message);
      }
   }

   public static void send(Actor actor, Translatable translation) {
      String[] message = PROVIDER.translate(translation);
      if (message.length != 1 || !message[0].isEmpty()) {
         Validate.isTrue(actor.raw() instanceof CommandSender, "Actor not command sender", new Object[0]);
         _send((CommandSender)actor.raw(), message);
      }
   }

   public static void send(Collection<? extends CommandSender> players, String... message) {
      if (message.length != 1 || !message[0].isEmpty()) {
         players.forEach(player -> _send(player, message));
      }
   }

   private static void _send(CommandSender sender, String... messages) {
      boolean miniMessageEnabled = Cache.fetch("mini-message-hook", () -> getPlugin().getHookRegistry().isHookEnabled(MiniMessageHookProvider.class));
      if (miniMessageEnabled) {
         for (String line : messages) {
            if (line.contains("\u00a7")) {
               sender.sendMessage(messages);
               return;
            }
         }

         MiniMessageHook hook = getPlugin().getHookRegistry().getHook(MiniMessageHook.class);

         for (String line : messages) {
            hook.sendRichMessage(sender, line);
         }
      } else {
         sender.sendMessage(messages);
      }
   }

   public static void error(Actor actor, Throwable exception) {
      Validate.isTrue(actor.raw() instanceof CommandSender, "Actor not command sender", new Object[0]);
      error((CommandSender)actor.raw(), exception);
   }

   public static void error(CommandSender sender, Throwable exception) {
      send(sender, t("error-processing", "%message%", exception.getMessage()));
      getPlugin().getLogger().error("An error occurred while processing an action", exception);
   }

   public static String formatTime(long secondsIn) {
      long dayCount = TimeUnit.SECONDS.toDays(secondsIn);
      long secondsCount = secondsIn - TimeUnit.DAYS.toSeconds(dayCount);
      long hourCount = TimeUnit.SECONDS.toHours(secondsCount);
      secondsCount -= TimeUnit.HOURS.toSeconds(hourCount);
      long minutesCount = TimeUnit.SECONDS.toMinutes(secondsCount);
      secondsCount -= TimeUnit.MINUTES.toSeconds(minutesCount);
      StringBuilder sb = new StringBuilder();
      if (dayCount != 0L) {
         sb.append(String.format("%d %s, ", dayCount, dayCount == 1L ? t("time.day")[0] : t("time.days")[0]));
      }

      if (hourCount != 0L) {
         sb.append(String.format("%d %s, ", hourCount, hourCount == 1L ? t("time.hour")[0] : t("time.hours")[0]));
      }

      if (minutesCount != 0L) {
         sb.append(String.format("%d %s ", minutesCount, minutesCount == 1L ? t("time.minute")[0] : t("time.minutes")[0]));
      }

      if (secondsCount != 0L) {
         sb.append(String.format("%d %s", secondsCount, secondsCount == 1L ? t("time.second")[0] : t("time.seconds")[0]));
      }

      if (secondsIn == 0L) {
         sb.append(String.format("%s", "0 " + t("time.seconds")[0]));
      }

      return sb.toString();
   }

   public static PhoenixCrates getPlugin() {
      return PhoenixCrates.getLegacyInstance();
   }
}
