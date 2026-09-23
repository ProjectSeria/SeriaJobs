package id.seria.gacha.lib.xseries.inventory;

import id.seria.gacha.lib.xseries.AbstractReferencedClass;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public abstract class BukkitInventoryView extends AbstractReferencedClass<InventoryView> {
   public BukkitInventoryView() {
   }

   public abstract Inventory getTopInventory();

   public abstract Inventory getBottomInventory();

   public abstract HumanEntity getPlayer();

   public abstract InventoryType getType();

   public abstract void setItem(int var1, ItemStack var2);

   public abstract ItemStack getItem(int var1);

   public abstract void setCursor(ItemStack var1);

   public abstract ItemStack getCursor();

   public abstract int convertSlot(int var1);

   public abstract void close();

   public abstract int countSlots();

   public abstract String getTitle();
}
