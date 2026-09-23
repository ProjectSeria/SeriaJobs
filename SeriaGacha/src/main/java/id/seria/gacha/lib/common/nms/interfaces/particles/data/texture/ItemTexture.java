package id.seria.gacha.lib.common.nms.interfaces.particles.data.texture;

import org.bukkit.inventory.ItemStack;

public final class ItemTexture extends ParticleTexture {
   private final ItemStack itemStack;

   public ItemTexture(ItemStack itemStack) {
      super(itemStack == null ? null : itemStack.getType(), (byte)0);
      this.itemStack = itemStack;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }
}
