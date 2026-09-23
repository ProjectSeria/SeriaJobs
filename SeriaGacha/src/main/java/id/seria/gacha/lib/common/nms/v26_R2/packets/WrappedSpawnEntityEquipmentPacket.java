package id.seria.gacha.lib.common.nms.v26_R2.packets;

import com.mojang.datafixers.util.Pair;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeEquipableEntityImpl;
import java.util.List;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class WrappedSpawnEntityEquipmentPacket extends WrappedPacket {
   private final Packet<?> packet;

   public WrappedSpawnEntityEquipmentPacket(FakeEquipableEntityImpl entity, List<Pair<EquipmentSlot, ItemStack>> items) {
      this.packet = new ClientboundSetEquipmentPacket(entity.getNmsEntity().getId(), items);
   }

   @Generated
   @Override
   public Packet<?> getPacket() {
      return this.packet;
   }
}
