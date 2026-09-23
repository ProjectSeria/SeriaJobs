package id.seria.gacha.lib.book.internals;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public interface BookHelper {
   void setPages(BookMeta var1, BaseComponent[][] var2);

   void openBook(Player var1, ItemStack var2);

   default BaseComponent[] itemToComponents(ItemStack item) {
      return new BaseComponent[]{new TextComponent(this.itemToJson(item))};
   }

   default String itemToJson(ItemStack item) {
      return NmsUtil.itemToJson(item);
   }
}
