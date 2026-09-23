package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeItemDisplayEntity;
import net.minecraft.world.entity.Display.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeItemDisplayEntityImpl extends AbstractFakeDisplayEntityImpl implements FakeItemDisplayEntity {
   public FakeItemDisplayEntityImpl(JavaPlugin plugin, ItemDisplay nmsEntity) {
      super(plugin, FakeEntityType.ITEM_DISPLAY, nmsEntity);
   }

   public ItemDisplay getNmsEntity() {
      return (ItemDisplay)this.nmsEntity;
   }

   public org.bukkit.entity.ItemDisplay getBukkitEntity() {
      return (org.bukkit.entity.ItemDisplay)this.nmsEntity.getBukkitEntity();
   }

   @Override
   public ItemStack getItemStack() {
      return this.getBukkitEntity().getItemStack();
   }

   @Override
   public FakeItemDisplayEntity setItemStack(ItemStack itemStack) {
      this.getBukkitEntity().setItemStack(itemStack);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeItemDisplayEntity.ItemDisplayTransform getItemDisplayTransform() {
      org.bukkit.entity.ItemDisplay.ItemDisplayTransform transform = this.getBukkitEntity().getItemDisplayTransform();
      return transform == null ? FakeItemDisplayEntity.ItemDisplayTransform.NONE : FakeItemDisplayEntity.ItemDisplayTransform.valueOf(transform.name());
   }

   @Override
   public FakeItemDisplayEntity setItemDisplayTransform(FakeItemDisplayEntity.ItemDisplayTransform transform) {
      this.getBukkitEntity().setItemDisplayTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.valueOf(transform.name()));
      this.metadataChanged = true;
      return this;
   }
}
