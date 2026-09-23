package id.seria.gacha.lib.common.nms.v26_R2.packets;

import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeEntityImpl;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;

public class WrappedEntityEventPacket extends WrappedPacket {
   private final Packet<?> packet;

   public WrappedEntityEventPacket(FakeEntityImpl entity, byte event) {
      this.packet = new ClientboundEntityEventPacket(entity.getNmsEntity(), event);
   }

   @Generated
   @Override
   public Packet<?> getPacket() {
      return this.packet;
   }
}
