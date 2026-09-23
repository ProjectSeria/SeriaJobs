package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeInteractionEntity;
import net.minecraft.world.entity.Interaction;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeInteractionEntityImpl extends FakeEntityImpl implements FakeInteractionEntity {
   public FakeInteractionEntityImpl(JavaPlugin plugin, Interaction nmsEntity) {
      super(plugin, FakeEntityType.INTERACTION, nmsEntity);
   }

   public Interaction getNmsEntity() {
      return (Interaction)this.nmsEntity;
   }

   public org.bukkit.entity.Interaction getBukkitEntity() {
      return (org.bukkit.entity.Interaction)this.nmsEntity.getBukkitEntity();
   }

   @Override
   public float getInteractionWidth() {
      return this.getBukkitEntity().getInteractionWidth();
   }

   @Override
   public FakeInteractionEntity setInteractionWidth(float width) {
      this.getBukkitEntity().setInteractionWidth(width);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public float getInteractionHeight() {
      return this.getBukkitEntity().getInteractionHeight();
   }

   @Override
   public FakeInteractionEntity setInteractionHeight(float height) {
      this.getBukkitEntity().setInteractionHeight(height);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public boolean isResponsive() {
      return this.getBukkitEntity().isResponsive();
   }

   @Override
   public FakeInteractionEntity setResponsive(boolean responsive) {
      this.getBukkitEntity().setResponsive(responsive);
      this.metadataChanged = true;
      return this;
   }
}
