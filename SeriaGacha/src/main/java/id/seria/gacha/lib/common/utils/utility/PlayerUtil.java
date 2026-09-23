package id.seria.gacha.lib.common.utils.utility;

import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.sdk.utilities.Colors;
import java.util.Collection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlayerUtil {
   private PlayerUtil() {
   }

   public static void sendClickableLink(Player player, String message, String url) {
      player.closeInventory();
      BaseComponent[] components = TextComponent.fromLegacyText(Colors.replaceColorCodes(message));

      for (BaseComponent component : components) {
         component.setClickEvent(new ClickEvent(Action.OPEN_URL, url));
      }

      player.spigot().sendMessage(components);
   }

   public static void playSound(Location location, Collection<? extends Player> players, XSound sound, float volume, float pitch) {
      players.forEach(p -> p.playSound(location, sound.parseSound(), volume, pitch));
   }

   public static void playSound(Location location, Collection<? extends Player> players, Sound sound, float volume, float pitch) {
      players.forEach(p -> p.playSound(location, sound, volume, pitch));
   }

   public static void playSound(Collection<? extends Player> players, Sound sound, float volume, float pitch) {
      players.forEach(p -> p.playSound(p.getLocation(), sound, volume, pitch));
   }

   public static void playSound(Collection<? extends Player> players, XSound sound, float volume, float pitch) {
      players.forEach(p -> p.playSound(p.getLocation(), sound.parseSound(), volume, pitch));
   }

   public static void sendMessage(Collection<? extends Player> players, String... message) {
      players.forEach(p -> p.sendMessage(message));
   }

   public static boolean haveSomePermission(Player player, String... permissions) {
      for (String permission : permissions) {
         if (player.hasPermission(permission)) {
            return true;
         }
      }

      return false;
   }

   public static boolean haveSomePermission(Player player, Collection<String> permissions) {
      return haveSomePermission(player, permissions.toArray(new String[0]));
   }
}
