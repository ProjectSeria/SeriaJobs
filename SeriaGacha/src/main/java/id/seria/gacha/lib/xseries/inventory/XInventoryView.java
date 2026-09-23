package id.seria.gacha.lib.xseries.inventory;

import org.bukkit.inventory.InventoryView;

public final class XInventoryView {
   private static final boolean USE_INTERFACE = InventoryView.class.isInterface();

   private XInventoryView() {
   }

   public static BukkitInventoryView of(InventoryView inventoryView) {
      return USE_INTERFACE ? new NewInventoryView(inventoryView) : new OldInventoryView(inventoryView);
   }
}
