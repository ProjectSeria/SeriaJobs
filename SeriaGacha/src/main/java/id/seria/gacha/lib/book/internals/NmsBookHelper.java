package id.seria.gacha.lib.book.internals;

import id.seria.gacha.lib.book.UnsupportedVersionException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class NmsBookHelper implements BookHelper {
   private final boolean doubleHands = NmsUtil.major <= 1 && NmsUtil.minor >= 9;
   private final Field craftMetaBookField;
   private final Method chatSerializerA;
   private final Method craftMetaBookInternalAddPageMethod;
   private final Method entityPlayerOpenBook;
   private final Object[] hands;

   public NmsBookHelper() {
      try {
         Class<?> craftMetaBookClass = NmsUtil.getCraftClass("inventory.CraftMetaBook");
         this.craftMetaBookField = craftMetaBookClass.getDeclaredField("pages");
         this.craftMetaBookField.setAccessible(true);
         Method cmbInternalAddMethod = null;

         try {
            cmbInternalAddMethod = craftMetaBookClass.getDeclaredMethod("internalAddPage", String.class);
            cmbInternalAddMethod.setAccessible(true);
         } catch (NoSuchMethodException var10) {
         }

         this.craftMetaBookInternalAddPageMethod = cmbInternalAddMethod;
         Class<?> chatSerializer = NmsUtil.getNmsClass("IChatBaseComponent$ChatSerializer", "network.chat", false);
         if (chatSerializer == null) {
            chatSerializer = NmsUtil.getNmsClass("ChatSerializer", true);
         }

         this.chatSerializerA = chatSerializer.getDeclaredMethod("a", String.class);
         Class<?> entityPlayerClass = NmsUtil.getNmsClass("EntityPlayer", "server.level", true);
         Class<?> itemStackClass = NmsUtil.getNmsClass("ItemStack", "world.item", true);
         if (this.doubleHands) {
            Class<?> enumHandClass = NmsUtil.getNmsClass("EnumHand", "world", true);

            Method openBookMethod;
            try {
               openBookMethod = entityPlayerClass.getMethod("a", itemStackClass, enumHandClass);
            } catch (NoSuchMethodException e) {
               openBookMethod = entityPlayerClass.getMethod("openBook", itemStackClass, enumHandClass);
            }

            this.entityPlayerOpenBook = openBookMethod;
            this.hands = enumHandClass.getEnumConstants();
         } else {
            this.entityPlayerOpenBook = entityPlayerClass.getMethod("openBook", itemStackClass);
            this.hands = null;
         }
      } catch (Exception e) {
         throw new IllegalStateException("Cannot initiate reflections for " + NmsUtil.version, e);
      }
   }

   @Override
   public void setPages(BookMeta meta, BaseComponent[][] components) {
      try {
         List<Object> pages = (List<Object>)this.craftMetaBookField.get(meta);
         if (pages != null) {
            pages.clear();
         }

         for (BaseComponent[] c : components) {
            if (c != null) {
               String json = ComponentSerializer.toString(c);
               if (this.craftMetaBookInternalAddPageMethod != null) {
                  this.craftMetaBookInternalAddPageMethod.invoke(meta, json);
               } else {
                  pages.add(this.chatSerializerA.invoke(null, json));
               }
            }
         }
      } catch (Exception e) {
         throw new UnsupportedVersionException(e);
      }
   }

   @Override
   public void openBook(Player player, ItemStack book) {
      try {
         if (this.doubleHands) {
            this.entityPlayerOpenBook.invoke(NmsUtil.toNms(player), NmsUtil.nmsCopy(book), this.hands[0]);
         } else {
            this.entityPlayerOpenBook.invoke(NmsUtil.toNms(player), NmsUtil.nmsCopy(book));
         }
      } catch (Exception e) {
         throw new UnsupportedVersionException(e);
      }
   }
}
