package id.seria.gacha.lib.common.nms.interfaces.entity;

import org.bukkit.inventory.ItemStack;

public interface FakeItemDisplayEntity extends FakeDisplayEntity {
   ItemStack getItemStack();

   FakeItemDisplayEntity setItemStack(ItemStack var1);

   FakeItemDisplayEntity.ItemDisplayTransform getItemDisplayTransform();

   FakeItemDisplayEntity setItemDisplayTransform(FakeItemDisplayEntity.ItemDisplayTransform var1);

   enum ItemDisplayTransform {
      NONE,
      THIRDPERSON_LEFTHAND,
      THIRDPERSON_RIGHTHAND,
      FIRSTPERSON_LEFTHAND,
      FIRSTPERSON_RIGHTHAND,
      HEAD,
      GUI,
      GROUND,
      FIXED;

      ItemDisplayTransform() {
      }
   }
}
