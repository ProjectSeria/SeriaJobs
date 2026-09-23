package id.seria.gacha.lib.xseries.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

class NewInventoryView extends BukkitInventoryView {
   private final InventoryView view;

   public NewInventoryView(Object view) {
      this.view = (InventoryView)view;
   }

   @Override
   public Inventory getTopInventory() {
      return this.view.getTopInventory();
   }

   @Override
   public Inventory getBottomInventory() {
      return this.view.getBottomInventory();
   }

   @Override
   public HumanEntity getPlayer() {
      return this.view.getPlayer();
   }

   @Override
   public InventoryType getType() {
      return this.view.getType();
   }

   @Override
   public void setItem(int slot, ItemStack item) {
      this.view.setItem(slot, item);
   }

   @Override
   public ItemStack getItem(int slot) {
      return this.view.getItem(slot);
   }

   @Override
   public void setCursor(ItemStack item) {
      this.view.setCursor(item);
   }

   @Override
   public ItemStack getCursor() {
      return this.view.getCursor();
   }

   @Override
   public int convertSlot(int slot) {
      return this.view.convertSlot(slot);
   }

   @Override
   public void close() {
      this.view.close();
   }

   @Override
   public int countSlots() {
      return this.view.countSlots();
   }

   @Override
   public String getTitle() {
      return this.view.getTitle();
   }

   public InventoryView object() {
      return this.view;
   }
}
