package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeFrogEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.enums.FrogVariant;
import net.minecraft.world.entity.animal.frog.Frog;
import org.bukkit.craftbukkit.entity.CraftFrog;
import org.bukkit.entity.Frog.Variant;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeFrogEntityImpl extends FakeLivingEntityImpl implements FakeFrogEntity {
   public FakeFrogEntityImpl(JavaPlugin plugin, Frog nmsEntity) {
      super(plugin, FakeEntityType.FROG, nmsEntity);
   }

   public Frog getNmsEntity() {
      return (Frog)this.nmsEntity;
   }

   @Override
   public FakeFrogEntity setVariant(FrogVariant variant) {
      CraftFrog frog = (CraftFrog)this.nmsEntity.getBukkitEntity();
      frog.setVariant(Variant.valueOf(variant.name()));
      return this;
   }

   @Override
   public FrogVariant getVariant() {
      CraftFrog frog = (CraftFrog)this.nmsEntity.getBukkitEntity();
      return FrogVariant.valueOf(frog.getVariant().name());
   }
}
