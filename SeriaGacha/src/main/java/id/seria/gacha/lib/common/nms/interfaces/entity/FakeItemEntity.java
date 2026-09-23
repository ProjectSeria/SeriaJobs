package id.seria.gacha.lib.common.nms.interfaces.entity;

import org.bukkit.inventory.ItemStack;

public interface FakeItemEntity extends FakeEntity {
   ItemStack getItemStack();

   void setItemStack(ItemStack var1);

   int getPickupDelay();

   void setPickupDelay(int var1);
}
