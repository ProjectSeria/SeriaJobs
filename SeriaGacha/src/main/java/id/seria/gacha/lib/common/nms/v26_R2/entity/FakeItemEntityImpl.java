package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeItemEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeItemEntityImpl extends FakeEntityImpl implements FakeItemEntity {
   public FakeItemEntityImpl(JavaPlugin plugin, ItemEntity nmsEntity) {
      super(plugin, FakeEntityType.DROPPED_ITEM, nmsEntity);
   }

   @Override
   public ItemStack getItemStack() {
      return CraftItemStack.asCraftMirror(this.getNmsEntity().getItem());
   }

   @Override
   public void setItemStack(ItemStack stack) {
      this.getNmsEntity().setItem(CraftItemStack.asNMSCopy(stack));
      this.metadataChanged = true;
   }

   @Override
   public int getPickupDelay() {
      return this.getNmsEntity().pickupDelay;
   }

   @Override
   public void setPickupDelay(int delay) {
      this.getNmsEntity().pickupDelay = Math.min(delay, 32767);
      this.metadataChanged = true;
   }

   public ItemEntity getNmsEntity() {
      return (ItemEntity)this.nmsEntity;
   }
}
