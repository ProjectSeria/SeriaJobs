package id.seria.gacha.limbo;

import id.seria.gacha.api.internal.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Operations {
   public Operations() {
   }

   public static <T> Optional<T> o(T obj) {
      return Optional.ofNullable(obj);
   }

   public static ItemStack safeBuild(Item item) {
      return o(item).map(Item::build).orElse(new ItemStack(Material.AIR));
   }

   public static String safeName(Item item) {
      return o(item).map(Item::getDisplayName).orElse("\u00a77Empty");
   }

   public static List<String> safeLore(Item item) {
      return o(item).map(Item::getLore).orElse(new ArrayList<>());
   }
}
