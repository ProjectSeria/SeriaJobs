package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeBlockDisplayEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import net.minecraft.world.entity.Display.BlockDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeBlockDisplayEntityImpl extends AbstractFakeDisplayEntityImpl implements FakeBlockDisplayEntity {
   public FakeBlockDisplayEntityImpl(JavaPlugin plugin, BlockDisplay nmsEntity) {
      super(plugin, FakeEntityType.BLOCK_DISPLAY, nmsEntity);
   }

   public BlockDisplay getNmsEntity() {
      return (BlockDisplay)this.nmsEntity;
   }

   public org.bukkit.entity.BlockDisplay getBukkitEntity() {
      return (org.bukkit.entity.BlockDisplay)this.nmsEntity.getBukkitEntity();
   }

   @Override
   public String getBlockData() {
      return this.getBukkitEntity().getBlock().getAsString();
   }

   @Override
   public FakeBlockDisplayEntity setBlockData(String blockData) {
      return this.apply(Bukkit.createBlockData(blockData));
   }

   @Override
   public FakeBlockDisplayEntity setBlock(Material material) {
      return this.apply(material.createBlockData());
   }

   private FakeBlockDisplayEntity apply(BlockData blockData) {
      this.getBukkitEntity().setBlock(blockData);
      this.metadataChanged = true;
      return this;
   }
}
