package id.seria.gacha.lib.common.nms.v26_R2.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEquipableEntity;
import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedSpawnEntityEquipmentPacket;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeEquipableEntityImpl extends FakeLivingEntityImpl implements FakeEquipableEntity {
   protected boolean equipmentChanged;

   public FakeEquipableEntityImpl(JavaPlugin plugin, FakeEntityType type, ArmorStand nmsEntity) {
      super(plugin, type, nmsEntity);
   }

   public FakeEquipableEntityImpl(JavaPlugin plugin, FakeEntityType type, Mob nmsEntity) {
      super(plugin, type, nmsEntity);
   }

   @Override
   public org.bukkit.inventory.ItemStack getItemInHand() {
      return this.nmsEntity instanceof ArmorStand
         ? this.toBukkit(((ArmorStand)this.nmsEntity).getItemBySlot(EquipmentSlot.MAINHAND))
         : this.toBukkit(((Mob)this.nmsEntity).getItemBySlot(EquipmentSlot.MAINHAND));
   }

   @Override
   public org.bukkit.inventory.ItemStack getItemInOffHand() {
      return this.nmsEntity instanceof ArmorStand
         ? this.toBukkit(((ArmorStand)this.nmsEntity).getItemBySlot(EquipmentSlot.OFFHAND))
         : this.toBukkit(((Mob)this.nmsEntity).getItemBySlot(EquipmentSlot.OFFHAND));
   }

   @Override
   public org.bukkit.inventory.ItemStack getHelmet() {
      return this.nmsEntity instanceof ArmorStand
         ? this.toBukkit(((ArmorStand)this.nmsEntity).getItemBySlot(EquipmentSlot.HEAD))
         : this.toBukkit(((Mob)this.nmsEntity).getItemBySlot(EquipmentSlot.HEAD));
   }

   @Override
   public org.bukkit.inventory.ItemStack getChestplate() {
      return this.nmsEntity instanceof ArmorStand
         ? this.toBukkit(((ArmorStand)this.nmsEntity).getItemBySlot(EquipmentSlot.CHEST))
         : this.toBukkit(((Mob)this.nmsEntity).getItemBySlot(EquipmentSlot.CHEST));
   }

   @Override
   public org.bukkit.inventory.ItemStack getLeggings() {
      return this.nmsEntity instanceof ArmorStand
         ? this.toBukkit(((ArmorStand)this.nmsEntity).getItemBySlot(EquipmentSlot.LEGS))
         : this.toBukkit(((Mob)this.nmsEntity).getItemBySlot(EquipmentSlot.LEGS));
   }

   @Override
   public org.bukkit.inventory.ItemStack getBoots() {
      return this.nmsEntity instanceof ArmorStand
         ? this.toBukkit(((ArmorStand)this.nmsEntity).getItemBySlot(EquipmentSlot.FEET))
         : this.toBukkit(((Mob)this.nmsEntity).getItemBySlot(EquipmentSlot.FEET));
   }

   public FakeLivingEntityImpl setItemInHand(org.bukkit.inventory.ItemStack item) {
      if (this.nmsEntity instanceof ArmorStand) {
         ((ArmorStand)this.nmsEntity).setItemSlot(EquipmentSlot.MAINHAND, this.toNMS(item));
      } else {
         ((Mob)this.nmsEntity).setItemSlot(EquipmentSlot.MAINHAND, this.toNMS(item));
      }

      this.equipmentChanged = true;
      return this;
   }

   public FakeLivingEntityImpl setItemInOffHand(org.bukkit.inventory.ItemStack item) {
      if (this.nmsEntity instanceof ArmorStand) {
         ((ArmorStand)this.nmsEntity).setItemSlot(EquipmentSlot.OFFHAND, this.toNMS(item));
      } else {
         ((Mob)this.nmsEntity).setItemSlot(EquipmentSlot.OFFHAND, this.toNMS(item));
      }

      this.equipmentChanged = true;
      return this;
   }

   public FakeLivingEntityImpl setHelmet(org.bukkit.inventory.ItemStack item) {
      if (this.nmsEntity instanceof ArmorStand) {
         ((ArmorStand)this.nmsEntity).setItemSlot(EquipmentSlot.HEAD, this.toNMS(item));
      } else {
         ((Mob)this.nmsEntity).setItemSlot(EquipmentSlot.HEAD, this.toNMS(item));
      }

      this.equipmentChanged = true;
      return this;
   }

   public FakeLivingEntityImpl setChestplate(org.bukkit.inventory.ItemStack item) {
      if (this.nmsEntity instanceof ArmorStand) {
         ((ArmorStand)this.nmsEntity).setItemSlot(EquipmentSlot.CHEST, this.toNMS(item));
      } else {
         ((Mob)this.nmsEntity).setItemSlot(EquipmentSlot.CHEST, this.toNMS(item));
      }

      this.equipmentChanged = true;
      return this;
   }

   public FakeLivingEntityImpl setLeggings(org.bukkit.inventory.ItemStack item) {
      if (this.nmsEntity instanceof ArmorStand) {
         ((ArmorStand)this.nmsEntity).setItemSlot(EquipmentSlot.LEGS, this.toNMS(item));
      } else {
         ((Mob)this.nmsEntity).setItemSlot(EquipmentSlot.LEGS, this.toNMS(item));
      }

      this.equipmentChanged = true;
      return this;
   }

   public FakeLivingEntityImpl setBoots(org.bukkit.inventory.ItemStack item) {
      if (this.nmsEntity instanceof ArmorStand) {
         ((ArmorStand)this.nmsEntity).setItemSlot(EquipmentSlot.FEET, this.toNMS(item));
      } else {
         ((Mob)this.nmsEntity).setItemSlot(EquipmentSlot.FEET, this.toNMS(item));
      }

      this.equipmentChanged = true;
      return this;
   }

   private org.bukkit.inventory.ItemStack toBukkit(ItemStack itemstack) {
      return CraftItemStack.asBukkitCopy(itemstack);
   }

   private ItemStack toNMS(org.bukkit.inventory.ItemStack itemstack) {
      return CraftItemStack.asNMSCopy(itemstack);
   }

   @Override
   public FakePacket prepareEquipmentPacket() {
      List<Pair<EquipmentSlot, ItemStack>> items = Lists.newArrayList();

      for (EquipmentSlot slot : EquipmentSlot.values()) {
         ItemStack equipment = this.getNmsEntity().getItemBySlot(slot);
         items.add(new Pair(slot, equipment));
      }

      return new WrappedSpawnEntityEquipmentPacket(this, items);
   }

   @Override
   public boolean hasEquipmentChanged() {
      return this.equipmentChanged;
   }

   @Override
   public void setEquipmentChanged(boolean value) {
      this.equipmentChanged = value;
   }
}
