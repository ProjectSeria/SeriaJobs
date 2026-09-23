package id.seria.gacha.lib.common.utils.utility;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {
   private InventoryUtil() {
   }

   public static int getPositionRaw(int x, int y) {
      return y * 9 - (9 - x) - 1;
   }

   public static int[] getXYPosition(int positionRaw) {
      int position = positionRaw + 1;
      double temp = position / 9.0;
      int y = (int)Math.ceil(temp);
      int x = (int)Math.round((temp - (int)temp) * 9.0);
      return new int[]{x == 0 ? 9 : x, y};
   }

   public static int getInventorySpace(Inventory inventory) {
      return (int)Arrays.asList(inventory.getContents()).stream().filter(i -> i != null && i.getType() != Material.AIR).count();
   }

   public static List<ItemStack> getInventoryItems(Inventory inventory) {
      return Arrays.asList(inventory.getContents()).stream().filter(i -> i != null && i.getType() != Material.AIR).collect(Collectors.toList());
   }

   public static boolean hasAvaliableSlot(Player player) {
      return player.getInventory().firstEmpty() != -1;
   }
}
