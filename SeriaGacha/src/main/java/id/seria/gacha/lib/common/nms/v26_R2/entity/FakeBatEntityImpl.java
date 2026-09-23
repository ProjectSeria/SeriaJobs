package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeBatEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import net.minecraft.world.entity.ambient.Bat;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeBatEntityImpl extends FakeEquipableEntityImpl implements FakeBatEntity {
   public FakeBatEntityImpl(JavaPlugin plugin, Bat nmsEntity) {
      super(plugin, FakeEntityType.BAT, nmsEntity);
   }

   public Bat getNmsEntity() {
      return (Bat)this.nmsEntity;
   }

   @Override
   public FakeBatEntity setResting(boolean resting) {
      this.getNmsEntity().setResting(resting);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public boolean isResting() {
      return this.getNmsEntity().isResting();
   }
}
