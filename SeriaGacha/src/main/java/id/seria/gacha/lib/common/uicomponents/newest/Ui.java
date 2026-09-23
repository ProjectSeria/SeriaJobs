package id.seria.gacha.lib.common.uicomponents.newest;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.utilities.Strings;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class Ui {
   public Ui() {
   }

   public static ComponentData createData(int x, int y, XMaterial material, Translatable title, Translatable description) {
      return new ComponentData(InventoryUtil.getPositionRaw(x, y), material, title, description);
   }

   public static ComponentData createData(XMaterial material, Translatable title, Translatable description) {
      return new ComponentData(material, title, description);
   }

   public static ComponentData createData(int x, int y, ItemStack item, Translatable title, Translatable description) {
      return new ComponentData(InventoryUtil.getPositionRaw(x, y), item, title, description);
   }

   public static ComponentData createData(int x, int y, ItemBuilder item, Translatable title, Translatable description) {
      return new ComponentData(InventoryUtil.getPositionRaw(x, y), item.build(), title, description);
   }

   public static String smallCaps(String value) {
      return Strings.smallCaps(value);
   }

   public static List<String> smallCaps(List<String> values) {
      List<String> out = new ArrayList<>(values.size());

      for (String v : values) {
         out.add(Strings.smallCaps(v));
      }

      return out;
   }

   public static String centerTitle(String title) {
      StringBuilder result = new StringBuilder();
      int spaces = 27 - ChatColor.stripColor(title).length();

      for (int i = 0; i < spaces; i++) {
         result.append(" ");
      }

      return result.append(title).toString();
   }

   public static String hex(String hexColor, String defaultColor) {
      return hexColor;
   }
}
