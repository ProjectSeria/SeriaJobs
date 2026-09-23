package id.seria.gacha.lib.common.nms.v26_R2.packets;

import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeLivingEntityImpl;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;

public class WrappedEntityAttributesPacket extends WrappedPacket {
   private final Packet<?> packet;

   public WrappedEntityAttributesPacket(FakeLivingEntityImpl entity) {
      this.packet = new ClientboundUpdateAttributesPacket(entity.getId(), entity.getNmsEntity().getAttributes().getSyncableAttributes());
   }

   @Generated
   @Override
   public Packet<?> getPacket() {
      return this.packet;
   }
}
