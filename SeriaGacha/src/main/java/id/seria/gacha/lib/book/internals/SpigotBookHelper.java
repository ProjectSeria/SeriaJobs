package id.seria.gacha.lib.book.internals;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Spigot;

public class SpigotBookHelper implements BookHelper {
   public SpigotBookHelper() {
   }

   @Override
   public void setPages(BookMeta meta, BaseComponent[][] components) {
      meta.spigot().setPages(components);
   }

   @Override
   public void openBook(Player player, ItemStack book) {
      player.openBook(book);
   }

   public static boolean isSupported() {
      try {
         Player.class.getMethod("openBook", ItemStack.class);
         Spigot.class.getMethod("setPages", BaseComponent[][].class);
         return true;
      } catch (NoSuchMethodException e) {
         return false;
      }
   }
}
