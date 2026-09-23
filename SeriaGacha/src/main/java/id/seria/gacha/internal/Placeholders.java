package id.seria.gacha.internal;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.sdk.hooks.placeholderapi.PlaceholderHookProvider;
import id.seria.gacha.sdk.utilities.Colors;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Deprecated
public class Placeholders {
   public Placeholders() {
   }

   @Deprecated
   public static String setPlaceholders(String rawText) {
      return Translations.r(
         rawText,
         "%online%",
         Bukkit.getServer().getOnlinePlayers().size(),
         "%max_online%",
         Bukkit.getServer().getMaxPlayers(),
         "%plugin_version%",
         getPlugin().getCurrentVersion().get()
      );
   }

   @Deprecated
   public static String setPlaceholders(String rawText, Player player) {
      if (player == null) {
         return setPlaceholders(rawText);
      }

      String text = Translations.r(
         rawText,
         "%player%",
         player.getName(),
         "%online%",
         Bukkit.getServer().getOnlinePlayers().size(),
         "%max_online%",
         Bukkit.getServer().getMaxPlayers(),
         "%plugin_version%",
         getPlugin().getCurrentVersion().get()
      );
      if (isPapiEnabled()) {
         text = Colors.replaceColorCodes(PlaceholderAPI.setPlaceholders(player, text));
      }

      return text;
   }

   @Deprecated
   public static String[] setPlaceholders(String[] rawArray, Player player) {
      String[] array = Translations.r(
         rawArray,
         "%player%",
         player.getName(),
         "%online%",
         Bukkit.getServer().getOnlinePlayers().size(),
         "%max_online%",
         Bukkit.getServer().getMaxPlayers(),
         "%plugin_version%",
         getPlugin().getCurrentVersion().get()
      );
      if (isPapiEnabled()) {
         for (int i = 0; i < array.length; i++) {
            array[i] = Colors.replaceColorCodes(PlaceholderAPI.setPlaceholders(player, array[i]));
         }
      }

      return array;
   }

   @Deprecated
   public static List<String> setPlaceholders(List<String> rawList) {
      List<String> list = Translations.r(
         rawList,
         "%online%",
         Bukkit.getServer().getOnlinePlayers().size(),
         "%max_online%",
         Bukkit.getServer().getMaxPlayers(),
         "%plugin_version%",
         getPlugin().getCurrentVersion().get()
      );
      if (isPapiEnabled()) {
         for (int i = 0; i < list.size(); i++) {
            list.set(i, Colors.replaceColorCodes(PlaceholderAPI.setPlaceholders(null, list.get(i))));
         }
      }

      return list;
   }

   @Deprecated
   public static List<String> setPlaceholders(List<String> rawList, Player player) {
      if (player == null) {
         return setPlaceholders(rawList);
      }

      List<String> list = Translations.r(
         rawList,
         "%player%",
         player.getName(),
         "%online%",
         Bukkit.getServer().getOnlinePlayers().size(),
         "%max_online%",
         Bukkit.getServer().getMaxPlayers(),
         "%plugin_version%",
         getPlugin().getCurrentVersion().get()
      );
      if (isPapiEnabled()) {
         for (int i = 0; i < list.size(); i++) {
            list.set(i, Colors.replaceColorCodes(PlaceholderAPI.setPlaceholders(player, list.get(i))));
         }
      }

      return list;
   }

   private static boolean isPapiEnabled() {
      return getPlugin().getHookRegistry().isHookEnabled(PlaceholderHookProvider.class);
   }

   private static PhoenixCrates getPlugin() {
      return PhoenixCrates.getLegacyInstance();
   }
}
