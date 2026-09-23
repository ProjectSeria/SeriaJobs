package id.seria.gacha.lib.common.nms.interfaces.entity;

import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;
import org.bukkit.inventory.ItemStack;

public interface FakeEquipableEntity extends FakeLivingEntity {
   ItemStack getItemInHand();

   ItemStack getItemInOffHand();

   ItemStack getHelmet();

   ItemStack getChestplate();

   ItemStack getLeggings();

   ItemStack getBoots();

   FakeLivingEntity setItemInHand(ItemStack var1);

   FakeLivingEntity setItemInOffHand(ItemStack var1);

   FakeLivingEntity setHelmet(ItemStack var1);

   FakeLivingEntity setChestplate(ItemStack var1);

   FakeLivingEntity setLeggings(ItemStack var1);

   FakeLivingEntity setBoots(ItemStack var1);

   FakePacket prepareEquipmentPacket();
}
